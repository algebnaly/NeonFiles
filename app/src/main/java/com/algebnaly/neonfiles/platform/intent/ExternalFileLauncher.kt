package com.algebnaly.neonfiles.platform.intent

import android.content.Context
import android.content.Intent
import com.algebnaly.neonfiles.filesystem.utils.toContentUri
import java.nio.file.Path

fun openWithExternalApplication(
    context: Context,
    path: Path,
    mimeType: String,
) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(
            path.toContentUri(),
            mimeType.ifBlank { "*/*" },
        )
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(intent)
}