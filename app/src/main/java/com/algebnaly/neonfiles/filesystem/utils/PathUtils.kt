package com.algebnaly.neonfiles.filesystem.utils

import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

fun Path.isDirectorySafe(): Boolean =
    this.fileSystem.provider().readAttributes<BasicFileAttributes>(
        this,
        BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS
    ).isDirectory