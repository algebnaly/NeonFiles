package com.algebnaly.neonfiles.filesystem.utils
import android.os.Environment
import java.nio.file.Path


fun getExternalRootPath(): Path {
    return Environment.getExternalStorageDirectory().toPath()
}