package com.algebnaly.neonfiles.utils

import android.content.Context
import android.content.Intent
import com.algebnaly.neonfiles.filesystem.utils.toContentUri
import java.nio.file.Path

fun startApkInstallationIntent(context: Context, apkFile: Path) {
    val uri = apkFile.toContentUri()
    val installIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(installIntent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}