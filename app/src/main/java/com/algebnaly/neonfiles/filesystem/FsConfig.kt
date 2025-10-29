package com.algebnaly.neonfiles.filesystem

import kotlinx.serialization.Serializable

@Serializable
sealed class FsConfig {
    @Serializable
    object Local : FsConfig()

    @Serializable
    data class NFS(
        val serverAddress: String,
        val uid: Int = 1000,
        val gid: Int = 1000,
    ) : FsConfig()

    @Serializable
    data class SMB(
        val serverAddress: String,
        val username: String,
        val password: String,
        val principal: String? = null,
    ) : FsConfig()

    @Serializable
    data class WebDav(
        val serverAddress: String,
        val username: String,
        val password: String,
    ) : FsConfig()
}