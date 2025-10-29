package com.algebnaly.neonfiles.filesystem
import java.io.IOException
import java.nio.file.FileSystem

sealed interface IStorageSource {
    val id: String
    val displayName: String

    @Throws(IOException::class)
    suspend fun getFileSystem(): FileSystem
}

data class LocalSource(
    override val id: String = "local_storage",
    override val displayName: String = "内部存储"
) : IStorageSource {
    override suspend fun getFileSystem(): FileSystem {
        return java.nio.file.FileSystems.getDefault()
    }
}

data class NFSSource(
    override val id: String,
    override val displayName: String,
    val server: String,
    val path: String,
) : IStorageSource {
    override suspend fun getFileSystem(): FileSystem {
        TODO("实现NFS FileSystem的获取逻辑")
    }
}

data class SMBSource(
    override val id: String,
    override val displayName: String,
    val server: String,
    val share: String,
    val credentials: UserCredentials?
) : IStorageSource {
    override suspend fun getFileSystem(): FileSystem {
        TODO("实现SMB FileSystem的获取逻辑")
    }
}

data class WebDavSource(
    override val id: String,
    override val displayName: String,
    val url: String,
    val credentials: UserCredentials?
) : IStorageSource {
    override suspend fun getFileSystem(): FileSystem {
        TODO("实现WebDAV FileSystem的获取逻辑")
    }
}

data class UserCredentials(val username: String, val password: String)