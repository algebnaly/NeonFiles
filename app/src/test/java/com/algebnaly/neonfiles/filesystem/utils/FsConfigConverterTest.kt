package com.algebnaly.neonfiles.filesystem.utils

import com.algebnaly.neonfiles.filesystem.FsConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class FsConfigConverterTest {
    val converter = FsConfigConverter()
    @Test
    fun nfs_config_round_trip() {
        val original = FsConfig.NFS(serverAddress = "192.168.1.10")
        val encoded = converter.fromFsConfig(original)
        val decoded = converter.toFsConfig(encoded)

        assertEquals(original, decoded)
    }
}