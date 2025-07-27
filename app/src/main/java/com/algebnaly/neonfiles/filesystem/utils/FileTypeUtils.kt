package com.algebnaly.neonfiles.filesystem.utils

import android.webkit.MimeTypeMap

fun getMimeType(filePath: String): String? {
    val extension = MimeTypeMap.getFileExtensionFromUrl(filePath)
    return if (extension != null) {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    } else {
        null
    }
}

fun isImage(mime: String?): Boolean {
    return mime?.startsWith("image/") == true
}

fun isVideo(mime: String?): Boolean {
    return mime?.startsWith("video/") == true
}

fun isAudio(mime: String?): Boolean {
    return mime?.startsWith("audio/") == true
}

fun isText(mime: String?): Boolean {
    return mime?.startsWith("text/") == true
}