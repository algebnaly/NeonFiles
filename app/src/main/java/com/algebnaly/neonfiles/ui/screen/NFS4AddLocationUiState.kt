package com.algebnaly.neonfiles.ui.screen

import com.algebnaly.neonfiles.data.LocationItem
import com.algebnaly.neonfiles.filesystem.FsConfig
import com.algebnaly.neonfiles.filesystem.FsType

data class NFS4AddLocationUiState(
    val name: String = "New NFS4 Location",
    val serverAddress: String = "localhost",
    val serverPort: Short = 2049,
    val path: String = "/",
    val warningMessage: String = ""
){
    fun toLocationItem(): LocationItem{
        return LocationItem(
            name = name,
            path = path,
            fsType = FsType.NFS,
            fsConfig = FsConfig.NFS(
                serverAddress = serverAddress,
            )
        )
    }
}