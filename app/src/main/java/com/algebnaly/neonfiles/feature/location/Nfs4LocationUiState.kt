package com.algebnaly.neonfiles.feature.location

import com.algebnaly.neonfiles.core.model.StorageLocation
import com.algebnaly.neonfiles.filesystem.StorageConfig

data class Nfs4LocationUiState(
    val id: Int = 0,
    val name: String = "New NFS4 Location",
    val serverAddress: String = "localhost",
    val serverPort: Short = 2049,
    val path: String = "/",
    val warningMessage: String = ""
){
    val isEditMode: Boolean get() = id > 0

    fun toStorageLocation(): StorageLocation {
        return StorageLocation(
            id = id,
            name = name,
            path = path,
            config = StorageConfig.NFS(
                serverAddress = serverAddress,
            )
        )
    }
}