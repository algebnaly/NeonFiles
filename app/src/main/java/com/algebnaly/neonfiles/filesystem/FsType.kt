package com.algebnaly.neonfiles.filesystem

import com.algebnaly.nfs4c.NFS4FileSystemProvider

enum class FsType(val code: UShort) {
    Local(0u),
    NFS(1u),
    SMB(2u),
    WebDav(3u);

    companion object {
        private val map: Map<UShort, FsType> = entries.associateBy { it.code }
        fun fromU16(value: UShort): FsType =
            map[value] ?: error("Invalid FsType value: $value")
    }

    fun getFsScheme(): String =
        when (this) {
            Local -> "file"
            NFS -> NFS4FileSystemProvider.SCHEME
            SMB -> "smb"
            WebDav -> "webdav"
        }
}