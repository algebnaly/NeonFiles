package com.algebnaly.neonfiles.filesystem

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface StorageConfig {
    @Serializable
    @SerialName("local")
    object Local : StorageConfig

    @Serializable
    @SerialName("nfs")
    data class NFS(
        val serverAddress: String,
        val uid: Int = 1000,
        val gid: Int = 1000,
    ) : StorageConfig

    @Serializable
    @SerialName("smb")
    data class SMB(
        val serverAddress: String,
        val username: String,
        val password: String,
        val principal: String? = null,
    ) : StorageConfig

    @Serializable
    @SerialName("webdav")
    data class WebDav(
        val serverAddress: String,
        val username: String,
        val password: String,
    ) : StorageConfig
}