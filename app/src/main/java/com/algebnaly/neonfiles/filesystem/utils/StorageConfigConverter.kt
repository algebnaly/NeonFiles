package com.algebnaly.neonfiles.filesystem.utils

import androidx.room.TypeConverter
import com.algebnaly.neonfiles.filesystem.StorageConfig
import kotlinx.serialization.json.Json

class StorageConfigConverter {
    private val json = Json {
        classDiscriminator = "type"
    }

    @TypeConverter
    fun fromStorageConfig(config: StorageConfig): String =
        json.encodeToString(StorageConfig.serializer(), config)

    @TypeConverter
    fun toStorageConfig(value: String): StorageConfig =
        json.decodeFromString(StorageConfig.serializer(), value)
}