package com.algebnaly.neonfiles.filesystem.utils

import com.algebnaly.neonfiles.filesystem.StorageConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class StorageConfigConverterTest {
    val converter = StorageConfigConverter()
    @Test
    fun nfs_config_round_trip() {
        val original = StorageConfig.NFS(serverAddress = "192.168.1.10")
        val encoded = converter.fromStorageConfig(original)
        val decoded = converter.toStorageConfig(encoded)

        assertEquals(original, decoded)
    }
}