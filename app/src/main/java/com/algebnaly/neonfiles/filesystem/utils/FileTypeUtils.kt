package com.algebnaly.neonfiles.filesystem.utils

import android.webkit.MimeTypeMap

fun getMimeType(filePath: String): String? {
    val extension = filePath.substringAfterLast('.', "").lowercase()
    if (extension.isEmpty()) return null
    if (extension == "apk") return "application/vnd.android.package-archive"
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
}

fun getMimeTypeWithDefault(filePath: String): String {
    return getMimeType(filePath) ?: "*/*"
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

fun isApkFile(mime: String?): Boolean{
    return  mime?.startsWith("application/vnd.android.package-archive") == true
}