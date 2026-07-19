package com.algebnaly.neonfiles.core.filesystem
import com.algebnaly.neonfiles.core.model.StorageLocation
import com.algebnaly.neonfiles.filesystem.FsProvider
import com.algebnaly.neonfiles.filesystem.StorageConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.URI
import java.nio.file.FileSystemNotFoundException
import java.nio.file.Path
import java.nio.file.Paths

interface StorageConnector {
    suspend fun connect(location: StorageLocation): Path
}

class NioStorageConnector(
    private val fsProvider: FsProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : StorageConnector {

    override suspend fun connect(location: StorageLocation): Path =
        withContext(ioDispatcher) {
            when (val config = location.config) {
                StorageConfig.Local -> Paths.get(location.path)
                is StorageConfig.NFS -> connectNfs(location.path, config)
                is StorageConfig.SMB ->
                    error("SMB storage is not implemented")
                is StorageConfig.WebDav ->
                    error("WebDAV storage is not implemented")
            }
        }

    private fun connectNfs(path: String, config: StorageConfig.NFS): Path {
        val address = InetAddress.getByName(config.serverAddress).hostAddress
        val uri = URI.create("nfs4://$address$path")

        val fileSystem = try {
            fsProvider.nfs4FileSystemProvider.getFileSystem(uri)
        } catch (_: FileSystemNotFoundException) {
            fsProvider.nfs4FileSystemProvider.newFileSystem(
                uri,
                emptyMap<String, Any>(),
            )
        }

        return fileSystem.getPath(path)
    }
}