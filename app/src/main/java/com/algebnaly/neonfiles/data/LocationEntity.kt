package com.algebnaly.neonfiles.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.algebnaly.neonfiles.filesystem.StorageConfig
import com.algebnaly.neonfiles.filesystem.FsType

@Entity(tableName = "location_items")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String = "new location",
    val fsType: FsType = FsType.Local,
    val path: String = "/",

    val fsConfig: StorageConfig = StorageConfig.Local,
)
