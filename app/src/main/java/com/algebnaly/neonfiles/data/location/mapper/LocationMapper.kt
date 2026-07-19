package com.algebnaly.neonfiles.data.location.mapper

import com.algebnaly.neonfiles.core.model.StorageLocation
import com.algebnaly.neonfiles.data.LocationEntity
import com.algebnaly.neonfiles.filesystem.FsType
import com.algebnaly.neonfiles.filesystem.StorageConfig

private fun StorageConfig.toFsType(): FsType = when (this) {
    StorageConfig.Local -> FsType.Local
    is StorageConfig.NFS -> FsType.NFS
    is StorageConfig.SMB -> FsType.SMB
    is StorageConfig.WebDav -> FsType.WebDav
}

fun LocationEntity.toDomain() = StorageLocation(
    id = id,
    name = name,
    path = path,
    config = fsConfig,
)

fun StorageLocation.toEntity() = LocationEntity(
    id = id,
    name = name,
    fsType = config.toFsType(),
    path = path,
    fsConfig = config,
)