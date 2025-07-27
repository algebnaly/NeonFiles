package com.algebnaly.neonfiles.filesystem.utils
import android.os.Environment
import java.io.File


fun getExternalRootPath(): File {
    return Environment.getExternalStorageDirectory()
}