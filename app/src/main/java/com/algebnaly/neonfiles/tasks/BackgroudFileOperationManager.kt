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

sealed class BackgroundFileOperationManagerInfo {
    data class Ok(val message: String) : BackgroundFileOperationManagerInfo()
    data class CopyOk(val targetDir: Path) : BackgroundFileOperationManagerInfo()
    data class Err(val message: String) : BackgroundFileOperationManagerInfo()
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
        val job = scope.launch(Dispatchers.IO) {
            for (f in fileSet) {
                try {
                    ensureActive()
                        copyToDir(f, targetDir, onProgress = {
                            p ->
                            taskManager.onProgress(p, taskId)
                        }
                        )
                }  catch (_: CancellationException){
                    // task cancelled, do nothing
                    // TODO: raise a toast that info this cancellation
                }
                catch (e: Exception) {
                    _eventFlow.emit(BackgroundFileOperationManagerInfo.Err(e.toString()))
                    throw e
                }
            }
            _eventFlow.emit(BackgroundFileOperationManagerInfo.CopyOk(targetDir))
            taskManager.removeTask(taskId)

        }
        taskManager.addTask(taskId, TaskInfo(
            name = "copy $fileSet to $targetDir",// TODO: i18n
            job = job,
            progression = 0f
        ))
    }

    @OptIn(ExperimentalPathApi::class)
    fun doDelete(fileSet: Set<Path>) {
        scope.launch {
            try {
                for (f in fileSet) {
                    f.deleteRecursively()
                }
            } catch (e: Exception) {
                _eventFlow.emit(BackgroundFileOperationManagerInfo.Err(e.toString()))
            }
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private suspend fun copyToDir(src: Path, dst: Path, onProgress: (Float) -> Unit) {
        val realTarget = dst.resolve(src.fileName.toString())
        if (isSubDirectory(
                src,
                realTarget,
                includeSelf = true
            )
        ) { //copy a directory to its sub Directory is not allowed
            return
        }
        if (src.fileSystem == dst.fileSystem) {
            when (src.fileSystem.provider().scheme) {
                "file" -> {
                    // links are not support for now
                    src.copyToRecursively(
                        target = realTarget,
                        followLinks = false,
                        onError = { s, d, e ->
                            scope.launch {
                                Log.d("neonFilesDebug", e.toString())
                                _eventFlow.emit(BackgroundFileOperationManagerInfo.Err(e.toString()))
                            }
                            OnErrorResult.TERMINATE
                        })
                }

                "nfs4" -> {
                    // TODO server side coping
                    copyRecursivelySimple(src, dst, onProgress = onProgress,onError = { p1, p2, p3 ->
                        CopyOnErrorOperation.Terminate
                    })
                }

                else -> {
                    copyRecursivelySimple(src, dst, onProgress = onProgress,onError = { p1, p2, p3 ->
                        CopyOnErrorOperation.Terminate
                    })
                }
            }
        } else {
            copyRecursivelySimple(src, realTarget,onProgress = onProgress, onError = { p1, p2, p3 ->
                CopyOnErrorOperation.Terminate
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
    onProgress: (newProgress: Float) -> Unit,
    onError: (s: Path, d: Path, e: Exception) -> CopyOnErrorOperation
): Unit = withContext(Dispatchers.IO) {
    ensureActive()

    try {
        if (src.isRegularFile()) {
            copyStreamLike(src, dst, onProgress = onProgress, onError)
            return@withContext
        }

        if (!dst.exists()) {
            Files.createDirectories(dst)
        }

        Files.list(src).use { stream ->
            for (child in stream) {
                ensureActive()

                val target = dst.resolve(child.fileName)
                try {
                    copyRecursivelySimple(child, target, onProgress=onProgress, onError = onError)
                } catch (e: Exception) {
                    when (onError(child, target, e)) {
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
    onProgress: (Float) -> Unit,
    onError: (s: Path, d: Path, e: Exception) -> CopyOnErrorOperation
) = withContext(Dispatchers.IO) {
        try {
            dst.parent?.let { parent ->
                if (!parent.exists()) {
                    Files.createDirectories(parent)
                }
            }

            // 使用流复制文件内容
            val chl = src.fileSystem.provider().newByteChannel(src, setOf(StandardOpenOption.READ))
            val totalSize = chl.size()
            var writtenBytes = 0
            chl.use { channel ->
                FileChannel.open(dst, StandardOpenOption.WRITE, StandardOpenOption.CREATE).use { out ->
                    val buffer = ByteBuffer.allocateDirect(1024 * 1024)
                    while (channel.read(buffer) > 0) {
                        ensureActive()
                        buffer.flip()
                        writtenBytes += out.write(buffer)
                        if(totalSize > 0){
                            onProgress(writtenBytes.toFloat() / totalSize)
                        }else{
                            onProgress(1f)
                        }
                        buffer.clear()
                    }
                }
            }

            try {
                val attrs = Files.readAttributes(src, BasicFileAttributes::class.java)
                Files.setLastModifiedTime(dst, attrs.lastModifiedTime())
            } catch (e: Exception) {
                Log.d("neonFilesDebug", "setAttr Error")
                throw e
                TODO("Not Implemented: " + e.toString())
            }

        } catch (_: CancellationException){
            //TODO: info this cancellation
            // close files here
        }
        catch (e: Exception) {
            onError(src, dst, e)
        }
}