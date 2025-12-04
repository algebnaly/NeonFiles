package com.algebnaly.neonfiles.tasks

import android.util.Log
import com.algebnaly.neonfiles.filesystem.utils.CopyOnErrorOperation
import com.algebnaly.neonfiles.filesystem.utils.isDirectorySafe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes
import java.util.UUID
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.OnErrorResult
import kotlin.io.path.absolute
import kotlin.io.path.copyToRecursively
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

enum class OperationType {
    Copy,
    Cut,
    Delete
}

sealed class BackgroundFileOperationManagerInfo {
    data class Ok(val type: OperationType, val message: String) :
        BackgroundFileOperationManagerInfo()

    data class Err(val type: OperationType, val message: String) :
        BackgroundFileOperationManagerInfo()

    data class Cancel(val type: OperationType) : BackgroundFileOperationManagerInfo()
}


class BackgroundFileOperationManager(
    private val scope: CoroutineScope,
) {
    private val _eventFlow =
        MutableSharedFlow<BackgroundFileOperationManagerInfo>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    val eventFlow = _eventFlow.asSharedFlow()
    val taskManager = TaskManager()

    fun doCopy(fileSet: Set<Path>, targetDir: Path) {
        if (!targetDir.isDirectorySafe()) {
            return
        }
        val taskId: UUID = UUID.randomUUID()
        var canceled = false
        var hasError = false
        val job = scope.launch(Dispatchers.IO) {
            for (f in fileSet) {
                try {
                    ensureActive()
                    copyToDirWithFsCheck(f, targetDir, onProgress = { p ->
                        taskManager.onProgress(p, taskId)
                    })
                } catch (_: CancellationException) {
                    canceled = true
                    // task cancelled, do nothing
                    // TODO: raise a toast that info this cancellation
                } catch (e: Exception) {
                    _eventFlow.emit(
                        BackgroundFileOperationManagerInfo.Err(
                            type = OperationType.Copy,
                            message = e.toString()
                        )
                    )
                    hasError = true
                }
            }
            if (canceled) {
                _eventFlow.emit(BackgroundFileOperationManagerInfo.Cancel(type = OperationType.Copy))
            } else if (!hasError) {
                _eventFlow.emit(
                    BackgroundFileOperationManagerInfo.Ok(
                        type = OperationType.Copy,
                        message = summary(fileSet)
                    )
                )
            }
            taskManager.removeTask(taskId)

        }
        taskManager.addTask(
            taskId, TaskInfo(
                name = "copy $fileSet to $targetDir",// TODO: i18n
                job = job,
                progressInfo = ProgressInfo(
                    current = 0,
                    total = 1
                )
            )
        )
    }

    @OptIn(ExperimentalPathApi::class)
    fun doDelete(fileSet: Set<Path>) {
        scope.launch {
            try {
                for (f in fileSet) {
                    f.deleteRecursively()
                }
                _eventFlow.emit(BackgroundFileOperationManagerInfo.Ok(
                    type = OperationType.Delete,
                    message = summary(fileSet)
                ))
            } catch (e: Exception) {
                _eventFlow.emit(
                    BackgroundFileOperationManagerInfo.Err(
                        type = OperationType.Delete,
                        message = e.toString()
                    )
                )
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private suspend fun copyToDirWithFsCheck(src: Path, dst: Path, onProgress: OnProgressType) {
        if (isSubDirectory(
                src,
                dst,
                includeSelf = true
            )
        ) { //copy a directory to its sub Directory is not allowed
            return
        }
        if (src.fileSystem == dst.fileSystem) {
            when (src.fileSystem.provider().scheme) {
                "file" -> {
                    val realTarget = dst.resolve(src.fileName.toString())
                    // links are not support for now
                    src.copyToRecursively(
                        target = realTarget,
                        followLinks = false,
                        onError = { s, d, e ->
                            scope.launch {
                                _eventFlow.emit(
                                    BackgroundFileOperationManagerInfo.Err(
                                        type = OperationType.Copy,
                                        message = e.toString()
                                    )
                                )
                            }
                            throw e
                        })
                }

                "nfs4" -> {
                    // TODO server side coping
                    copyRecursivelySimple(
                        src,
                        dst,
                        onProgress = onProgress,
                        onError = { s, d, e ->
                            throw e
                        })
                }

                else -> {
                    copyRecursivelySimple(
                        src,
                        dst,
                        onProgress = onProgress,
                        onError = { s, d, e ->
                            throw e
                        })
                }
            }
        } else {
            copyRecursivelySimple(
                src,
                dst,
                onProgress = onProgress,
                onError = { s, d, e ->
                    throw e
                })
        }
    }
}

fun isSubDirectory(src: Path, dst: Path, includeSelf: Boolean = true): Boolean {
    if (src.fileSystem != dst.fileSystem) {
        return false
    }
    val srcPathStr = src.absolute().toString().trimEnd(src.fileSystem.separator.single())
    val dstPathStr = dst.absolute().toString().trimEnd(dst.fileSystem.separator.single())

    if (dstPathStr.startsWith(srcPathStr)) {
        if (dstPathStr.length == srcPathStr.length) {
            return includeSelf
        }
    }
    return false
}

suspend fun copyRecursivelySimple(
    src: Path,
    dst: Path,
    onProgress: OnProgressType,
    onError: (s: Path, d: Path, e: Exception) -> CopyOnErrorOperation
): Unit = withContext(Dispatchers.IO) {
    ensureActive()
    try {
        if (src.isRegularFile()) {
            val targetFile = dst.resolve(src.fileName.toString())
            copyStreamLike(src, targetFile, onProgress = onProgress, onError)
            return@withContext
        }

        val newTarget = dst.resolve(src.fileName.toString())

        if (!newTarget.exists()) {
            Files.createDirectories(newTarget)
        }

        Files.list(src).use { stream ->
            for (child in stream) {
                ensureActive()
                try {
                    copyRecursivelySimple(child, newTarget, onProgress = onProgress, onError = onError)
                } catch (e: Exception) {
                    when (onError(child, newTarget, e)) {
                        CopyOnErrorOperation.Terminate -> return@withContext
                        CopyOnErrorOperation.Continue -> continue
                    }
                }
            }
        }
    } catch (e: Exception) {
        onError(src, dst, e)
    }
}

suspend fun copyStreamLike(
    src: Path,
    dst: Path,
    onProgress: OnProgressType,
    onError: (s: Path, d: Path, e: Exception) -> CopyOnErrorOperation
) = withContext(Dispatchers.IO) {
    try {
        dst.parent?.let { parent ->
            if (!parent.exists()) {
                Files.createDirectories(parent)
            }
        }

        val chl = src.fileSystem.provider().newByteChannel(src, setOf(StandardOpenOption.READ))
        val totalSize = chl.size()
        var writtenBytes = 0
        chl.use { channel ->
            dst.fileSystem.provider().newByteChannel(
                dst, setOf(
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            ).use { out ->
                val buffer = ByteBuffer.allocateDirect(1024 * 1024)
                while (channel.read(buffer) > 0) {
                    ensureActive()
                    buffer.flip()
                    writtenBytes += out.write(buffer)
                    if (totalSize > 0) {
                        onProgress(ProgressInfo(current = writtenBytes.toLong(), total = totalSize))
                    } else {
                        onProgress(ProgressInfo(0, 1))
                    }
                    buffer.clear()
                }
            }
        }

        try {
            val attrs = Files.readAttributes(src, BasicFileAttributes::class.java)
            Files.setLastModifiedTime(dst, attrs.lastModifiedTime())
        } catch (e: Exception) {
            throw e
        }

    } catch (_: CancellationException) {
        //TODO: info this cancellation
        // close files here
    } catch (e: Exception) {
        onError(src, dst, e)
    }
}

fun summary(fileSet: Set<Path>): String {
    return when(fileSet.size) {
        0 -> ""
        1 -> fileSet.first().name
        else -> fileSet.first().name + "..."
    }
}