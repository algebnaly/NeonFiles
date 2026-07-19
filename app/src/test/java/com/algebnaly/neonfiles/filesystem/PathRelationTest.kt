package com.algebnaly.neonfiles.filesystem

import com.algebnaly.neonfiles.tasks.isSubDirectory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Path

class PathRelationTest {
    @Test
    fun child_is_subdirectory() {
        assertTrue(
            isSubDirectory(
                Path.of("/storage/source"),
                Path.of("/storage/source/child"),
            )
        )
    }

    @Test
    fun same_path_respects_includeSelf() {
        val path = Path.of("/storage/source")

        assertTrue(isSubDirectory(path, path, includeSelf = true))
        assertFalse(isSubDirectory(path, path, includeSelf = false))
    }

    @Test
    fun common_string_prefix_is_not_a_child() {
        assertFalse(
            isSubDirectory(
                Path.of("/storage/foo"),
                Path.of("/storage/foobar"),
            )
        )
    }
}