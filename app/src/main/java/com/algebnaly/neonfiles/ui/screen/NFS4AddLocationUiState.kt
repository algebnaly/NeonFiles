package com.algebnaly.neonfiles.ui.screen

import com.algebnaly.neonfiles.core.model.StorageLocation
import com.algebnaly.neonfiles.filesystem.StorageConfig

data class NFS4AddLocationUiState(
    val name: String = "New NFS4 Location",
    val serverAddress: String = "localhost",
    val serverPort: Short = 2049,
    val path: String = "/",
    val warningMessage: String = ""
) {
    fun toStorageLocation(): StorageLocation {
        return StorageLocation(
            name = name,
            path = path,
            config = StorageConfig.NFS(
                serverAddress = serverAddress,
            )
        )
    }
}