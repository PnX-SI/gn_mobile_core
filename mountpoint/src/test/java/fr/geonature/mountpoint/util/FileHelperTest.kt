package fr.geonature.mountpoint.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

/**
 * Unit test for `FileHelper`.
 *
 * @author S. Grimault
 */
class FileHelperTest {

    @Test
    fun `should create file from current file path`() {
        val parentDirectory = Files
            .createTempDirectory("tests_")
            .toFile()
        val newFile = parentDirectory.getFile("myFile.txt")
        newFile.createNewFile()

        assertTrue(newFile.exists())
        assertEquals(
            "${parentDirectory.absolutePath}/myFile.txt",
            newFile.absolutePath
        )

        parentDirectory.deleteRecursively()
    }

    @Test
    fun `should create file with relative path as children from current file path`() {
        val parentDirectory = Files
            .createTempDirectory("tests_")
            .toFile()
        val newFile = parentDirectory.getFile(
            "a",
            "b",
            "myFile.txt"
        )
        newFile.createNewFile()

        assertTrue(newFile.exists())
        assertEquals(
            "${parentDirectory.absolutePath}/a/b/myFile.txt",
            newFile.absolutePath
        )

        parentDirectory.deleteRecursively()
    }
}