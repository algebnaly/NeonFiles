package com.algebnaly.neonfiles.data

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.algebnaly.neonfiles.filesystem.FsConfig
import com.algebnaly.neonfiles.filesystem.FsProvider
import com.algebnaly.neonfiles.filesystem.FsType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.FileSystemNotFoundException
import java.nio.file.Path
import kotlin.collections.emptyMap
import java.net.InetAddress

@Entity(tableName = "location_items")
data class LocationItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String = "new location",
    val fsType: FsType = FsType.Local,
    val path: String = "/",

    val fsConfig: FsConfig = FsConfig.Local,
) {
    suspend fun toPath(fsProvider: FsProvider): Path = when (fsType) {
        FsType.Local -> File(path).toPath()
        FsType.NFS -> {
            val serverAddress = (fsConfig as FsConfig.NFS).serverAddress
            val scheme = fsType.getFsScheme()
            val address = withContext(Dispatchers.IO) {
                InetAddress.getByName(serverAddress)
            }
            val ip = address.hostAddress
            val uri = URI.create("$scheme://$ip$path")
            getOrCreateNfsFs(fsProvider, uri).getPath(path)
        }
        else -> throw NotImplementedError("toPath is not implemented for fsType: $fsType")
    }
    fun getOrCreateNfsFs(fsProvider: FsProvider, uri: URI, env: Map<String, Any> = emptyMap()): FileSystem =
        try {
            fsProvider.nfs4FileSystemProvider.getFileSystem(uri)
        } catch (_: FileSystemNotFoundException) {
            fsProvider.nfs4FileSystemProvider.newFileSystem(uri, env)
        }
}
