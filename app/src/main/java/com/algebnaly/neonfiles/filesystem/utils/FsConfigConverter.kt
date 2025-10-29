package com.algebnaly.neonfiles.filesystem.utils

import androidx.room.TypeConverter
import com.algebnaly.neonfiles.filesystem.FsConfig
import kotlinx.serialization.json.Json

class FsConfigConverter {
    private val json = Json {
        classDiscriminator = "type"
    }

    @TypeConverter
    fun fromFsConfig(config: FsConfig): String =
        json.encodeToString(FsConfig.serializer(), config)

    @TypeConverter
    fun toFsConfig(value: String): FsConfig =
        json.decodeFromString(FsConfig.serializer(), value)
}