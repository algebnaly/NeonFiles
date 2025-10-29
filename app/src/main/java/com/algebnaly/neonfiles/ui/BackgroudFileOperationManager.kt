package com.algebnaly.neonfiles.ui

import android.util.Log
import com.algebnaly.neonfiles.filesystem.utils.CopyOnErrorOperation
import com.algebnaly.neonfiles.filesystem.utils.isDirectorySafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.OnErrorResult
import kotlin.io.path.absolute
import kotlin.io.path.copyToRecursively
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.outputStream

sealed class BackgroundFileOperationManagerInfo {
    data class Ok(val message: String) : BackgroundFileOperationManagerInfo()
    data class CopyOk(val targetDir: Path) : BackgroundFileOperationManagerInfo()
    data class Err(val message: String) : BackgroundFileOperationManagerInfo()
}

class BackgroundFileOperationManager(
    private val scope: CoroutineScope,
    val onRefresh: () -> Unit = {}
) {

    private val _eventFlow =
        MutableSharedFlow<BackgroundFileOperationManagerInfo>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    val eventFlow = _eventFlow.asSharedFlow()

    fun doCopy(fileSet: Set<Path>, targetDir: Path) {
        if (!targetDir.isDirectorySafe()) {
            return
        }
        scope.launch {
            for (f in fileSet) {
                try {
                    Log.d("neonFilesDebug", "before copyToDir")
                    copyToDir(f, targetDir)
                } catch (e: Exception) {
                    _eventFlow.emit(BackgroundFileOperationManagerInfo.Err(e.toString()))
                }
            }
            _eventFlow.emit(BackgroundFileOperationManagerInfo.CopyOk(targetDir))
        }
        onRefresh()
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
        onRefresh()
    }

    @OptIn(ExperimentalPathApi::class)
    private suspend fun copyToDir(src: Path, dst: Path) {
        Log.d("neonFilesDebug", "before resolve")
        val realTarget = dst.resolve(src.fileName.toString())
        Log.d("neonFilesDebug", "after resolve")
        if (isSubDirectory(
                src,
                realTarget,
                includeSelf = true
            )
        ) { //copy a directory to its sub Directory is not allowed
            return
        }
        if (src.fileSystem == dst.fileSystem) {
            Log.d("neonFilesDebug", src.fileSystem.provider().scheme)
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
                    copyRecursivelySimple(src, dst, onError = { p1, p2, p3 ->
                        CopyOnErrorOperation.Terminate
                    })
                }

                else -> {
                    copyRecursivelySimple(src, dst, onError = { p1, p2, p3 ->
                        CopyOnErrorOperation.Terminate
                    })
                }
            }
        } else {
            Log.d("neonFilesDebug", "not the same filesystem")
            copyRecursivelySimple(src, dst, onError = { p1, p2, p3 ->
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

fun copyRecursivelySimple(
    src: Path,
    dst: Path,
    onError: (s: Path, d: Path, e: Exception) -> CopyOnErrorOperation
) {
    if (src.isRegularFile()) {
        Log.d("neonFilesDebug", "src is RegularFile")
        copyStreamLike(src, dst, onError = onError)
        return
    }
    if (src.isDirectorySafe()) {
        try {
            if (!dst.exists()) {
                Files.createDirectories(dst)
            }
        } catch (e: Exception) {
            when (onError(src, dst, e)) {
                CopyOnErrorOperation.Terminate -> return
                CopyOnErrorOperation.Continue -> return
            }
        }

        try {
            Files.walkFileTree(src, object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes
                ): FileVisitResult {
                    val relativePath = src.relativize(dir)
                    val targetDir = dst.resolve(relativePath)

                    try {
                        if (!targetDir.exists()) {
                            Files.createDirectory(targetDir)
                        }
                    } catch (e: Exception) {
                        return when (onError(dir, targetDir, e)) {
                            CopyOnErrorOperation.Terminate -> FileVisitResult.TERMINATE
                            CopyOnErrorOperation.Continue -> FileVisitResult.SKIP_SUBTREE
                        }
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val relativePath = src.relativize(file)
                    val targetFile = dst.resolve(relativePath)

                    try {
                        copyStreamLike(file, targetFile, onError = onError)
                    } catch (e: Exception) {
                        return when (onError(file, targetFile, e)) {
                            CopyOnErrorOperation.Terminate -> FileVisitResult.TERMINATE
                            CopyOnErrorOperation.Continue -> FileVisitResult.CONTINUE
                        }
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                    return when (onError(file, dst.resolve(src.relativize(file)), exc)) {
                        CopyOnErrorOperation.Terminate -> FileVisitResult.TERMINATE
                        CopyOnErrorOperation.Continue -> FileVisitResult.CONTINUE
                    }
                }
            })
        } catch (e: Exception) {
            onError(src, dst, e)
            Log.d("neonFilesDebug", e.toString())
        }
    }
}

fun copyStreamLike(
    src: Path,
    dst: Path,
    onError: (s: Path, d: Path, e: Exception) -> CopyOnErrorOperation
) {
    Log.d("NeonFilesDebug", "enter copyStreamLike")
    try {
        dst.parent?.let { parent ->
            if (!parent.exists()) {
                Files.createDirectories(parent)
            }
        }

        // 使用流复制文件内容
        src.inputStream().use { input ->
            dst.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        try {
            val attrs = Files.readAttributes(src, BasicFileAttributes::class.java)
            Files.setLastModifiedTime(dst, attrs.lastModifiedTime())
        } catch (e: Exception) {
            TODO("Not Implemented: " + e.toString())
        }

    } catch (e: Exception) {
        onError(src, dst, e)
    }
}