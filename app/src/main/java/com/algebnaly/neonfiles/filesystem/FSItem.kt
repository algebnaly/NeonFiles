package com.algebnaly.neonfiles.filesystem

import java.nio.file.Path

data class FsItem(
    val path: Path,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
)