package com.algebnaly.neonfiles.filesystem.utils

import android.net.Uri
import android.util.Log
import com.algebnaly.neonfiles.NeonFilesApplication
import com.algebnaly.neonfiles.data.AppContainer
import com.algebnaly.neonfiles.filesystem.FsProvider
import com.algebnaly.neonfiles.ui.NeonFilesAuthority
import com.algebnaly.nfs4c.NFS4FileSystemProvider
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.FileSystemNotFoundException

fun Path.isDirectorySafe(): Boolean =
    this.fileSystem.provider().readAttributes<BasicFileAttributes>(
        this,
        BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS
    ).isDirectory

fun uriToPath(uri: Uri): Path {
    val trueUri = extractTrueURI(uri)
    val fsProvider = NeonFilesApplication.instance.container.fsProvider

    val p = when (trueUri?.scheme) {
        "file" -> File(trueUri.path ?: "/").toPath()
        "nfs4" -> {
            try {
                fsProvider.nfs4FileSystemProvider.getPath(trueUri)
            }catch (e: FileSystemNotFoundException){
                throw e
            }catch (e: Exception){
                throw e
            }
        }
        else -> TODO("Not implemented")
    }
    return p
}

fun Uri.toNIOPath(): Path {
    return uriToPath(this)
}

fun Path.toContentUri(authority: String = NeonFilesAuthority): Uri {
    val pathString = this.toUri().toString()
    val cleanedPath = if (pathString.startsWith("/")) {
        pathString.substring(1)
    } else {
        pathString
    }
    return Uri.Builder()
        .scheme("content")
        .authority(authority)
        .appendPath(cleanedPath)
        .build()
}

fun Path.toContentUriString(authority: String = NeonFilesAuthority): String {
    return this.toContentUri(authority).toString()
}


fun extractTrueURI(encodedUri: Uri): URI? {

    val externalPath = encodedUri.path
    if (externalPath.isNullOrEmpty() || externalPath == "/") {
        return null
    }

    val decodedPath = URLDecoder.decode(externalPath.substring(1), StandardCharsets.UTF_8.name())

    return URI(decodedPath)
}