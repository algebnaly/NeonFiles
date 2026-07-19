package com.algebnaly.neonfiles.ui.screen

import com.algebnaly.neonfiles.core.model.StorageLocation
import com.algebnaly.neonfiles.filesystem.StorageConfig

data class NFS4EditLocationUiState(
    val id: Int = 0,
    val name: String = "",
    val serverAddress: String = "",
    val serverPort: Short = 2049,
    val path: String = "/",
    val warningMessage: String = ""
) {
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
