package com.algebnaly.neonfiles.platform.content

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import com.algebnaly.neonfiles.filesystem.utils.toNIOPath
import com.algebnaly.neonfiles.filesystem.utils.uriToPath
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.BasicFileAttributes

const val NeonFilesAuthority = "com.algebnaly.nfs4c.provider"

class NioContentProvider() : ContentProvider() {
    override fun delete(
        p0: Uri,
        p1: String?,
        p2: Array<out String?>?
    ): Int {
        TODO("Not yet implemented")
    }

    override fun getType(uri: Uri): String? {
        return try {
            val fileName = uri.lastPathSegment ?: return "application/octet-stream"
            val extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(fileName)
            android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) ?: "application/octet-stream"
        } catch (e: Exception) {
            Log.d("neonFiles", e.toString())
            "application/octet-stream"
        }
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String?>?,
        p2: String?,
        p3: Array<out String?>?,
        p4: String?
    ): Cursor? {
        // assume only one file for now
        val requestedProjection = projection ?: arrayOf(
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE
        )

        val cursor = MatrixCursor(requestedProjection)

        val nioPath: Path = uri.toNIOPath()

        try {
            val attributes: BasicFileAttributes =
                Files.readAttributes(nioPath, BasicFileAttributes::class.java)

            val row = cursor.newRow()

            for (column in requestedProjection) {
                when (column) {
                    OpenableColumns.DISPLAY_NAME -> {
                        row.add(nioPath.fileName.toString())
                    }

                    OpenableColumns.SIZE -> {
                        row.add(attributes.size())
                    }
                }
            }
        } catch (e: Exception) {
            // 处理 NIO/NFS 访问错误，例如文件不存在、网络中断等
            e.printStackTrace()
            // 最好记录错误并返回 null 或一个空 cursor
            return null
        }

        return cursor
    }

    override fun update(
        p0: Uri,
        p1: ContentValues?,
        p2: String?,
        p3: Array<out String?>?
    ): Int {
        TODO("Not yet implemented")
    }

    private var handlerThread: android.os.HandlerThread? = null
    private var backgroundHandler: android.os.Handler? = null

    private fun getBackgroundHandler(): android.os.Handler {
        if (backgroundHandler == null) {
            synchronized(this) {
                if (backgroundHandler == null) {
                    val thread = android.os.HandlerThread("NIOProvider-ProxyHandler").apply { start() }
                    handlerThread = thread
                    backgroundHandler = android.os.Handler(thread.looper)
                }
            }
        }
        return backgroundHandler!!
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") {
            throw IllegalArgumentException("Unsupported file mode: $mode")
        }

        val nioPath = uriToPath(uri)
        
        if (nioPath.fileSystem == java.nio.file.FileSystems.getDefault()) {
            return ParcelFileDescriptor.open(nioPath.toFile(), ParcelFileDescriptor.MODE_READ_ONLY)
        }

        val storageManager = context?.getSystemService(android.os.storage.StorageManager::class.java)

        if (storageManager != null) {
            val channel = nioPath.fileSystem.provider().newByteChannel(nioPath, setOf(StandardOpenOption.READ))
            return storageManager.openProxyFileDescriptor(
                ParcelFileDescriptor.MODE_READ_ONLY,
                object : android.os.ProxyFileDescriptorCallback() {
                    override fun onGetSize(): Long {
                        return Files.size(nioPath)
                    }

                    override fun onRead(offset: Long, size: Int, data: ByteArray): Int {
                        var totalRead = 0
                        channel.position(offset)
                        while (totalRead < size) {
                            val buffer = ByteBuffer.wrap(data, totalRead, size - totalRead)
                            val bytesRead = channel.read(buffer)
                            if (bytesRead < 0) break
                            totalRead += bytesRead
                        }
                        return totalRead
                    }

                    override fun onRelease() {
                        channel.close()
                    }
                },
                getBackgroundHandler()
            )
        } else {
            throw IllegalStateException("StorageManager not available or Android version too low")
        }
    }
}