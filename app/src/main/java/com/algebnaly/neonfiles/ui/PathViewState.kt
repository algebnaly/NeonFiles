package com.algebnaly.neonfiles.ui

import com.algebnaly.neonfiles.filesystem.utils.isDirectorySafe
import java.nio.file.Path

data class PathViewState(
    val path: Path,
    val name: String,
    val mimeType: String,
    val isDirectory: Boolean = path.isDirectorySafe(),
    val uniqueKey: String = path.toAbsolutePath().toString()
    )