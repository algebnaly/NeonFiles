package com.algebnaly.neonfiles.ui

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
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

const val NeonFilesAuthority = "com.algebnaly.nfs4c.provider"

class NIOContentProvider() : ContentProvider() {
    override fun delete(
        p0: Uri,
        p1: String?,
        p2: Array<out String?>?
    ): Int {
        TODO("Not yet implemented")
    }

    override fun getType(p0: Uri): String? {
        TODO("Not yet implemented")
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

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val nioPath = uriToPath(uri)
        val pipe = ParcelFileDescriptor.createPipe()
        val readFd = pipe[0]
        val writeFd = pipe[1]
        Thread {
            try {
                Files.newInputStream(nioPath).use { inputStream ->
                    FileOutputStream(writeFd.fileDescriptor).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            } finally {
                try {
                    writeFd.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                    throw e
                }
            }
        }.start()
        return readFd
    }
}