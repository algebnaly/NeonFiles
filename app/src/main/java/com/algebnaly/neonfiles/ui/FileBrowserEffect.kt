package com.algebnaly.neonfiles.ui

import java.nio.file.Path

sealed interface FileBrowserEffect {
    data class ShowMessage(val message: String) : FileBrowserEffect
    data class OpenExternal(val path: Path, val mimeType: String) : FileBrowserEffect
}