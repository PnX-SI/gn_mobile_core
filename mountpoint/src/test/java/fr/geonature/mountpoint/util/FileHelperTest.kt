package fr.geonature.mountpoint.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createTempDirectory

/**
 * Unit test for `FileHelper`.
 *
 * @author S. Grimault
 */
class FileHelperTest {

    private lateinit var rootDir: File

    @Before
    fun setUp() {
        rootDir = prepareTemporaryDirectoryStructure()
    }

    @After
    fun tearDown() {
        // deletes the temporary directory structure and all its contents
        rootDir.deleteRecursively()
    }

    @Test
    fun `should create file from current file path`() {
        val parentDirectory = Files.createTempDirectory("tests_")
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
        val parentDirectory = Files.createTempDirectory("tests_")
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

    @Test
    fun `should find existing file by absolute path`() {
        val expectedFile = File(
            rootDir,
            "file2.log"
        )
        val fileFound = rootDir.find(
            File(
                rootDir,
                "file2.log"
            ).absolutePath
        )
        assertNotNull(fileFound)
        assertEquals(
            expectedFile.absolutePath,
            fileFound?.absolutePath
        )
    }

    @Test
    fun `should find existing file by absolute nested path`() {
        val expectedFile = File(
            rootDir,
            "some/file3.txt"
        )
        val fileFound = rootDir.find(
            File(
                rootDir,
                "some/file3.txt"
            ).absolutePath
        )
        assertNotNull(fileFound)
        assertEquals(
            expectedFile.absolutePath,
            fileFound?.absolutePath
        )
    }

    @Test
    fun `should find existing file by relative path`() {
        val fileFound = rootDir.find("some/file3.txt")
        assertNotNull(fileFound)
        assertEquals(
            File(
                rootDir,
                "some/file3.txt"
            ).absolutePath,
            fileFound?.absolutePath
        )
    }

    @Test
    fun `should find existing deep nested file`() {
        val fileFound = rootDir.find("path/nested.txt")
        assertNotNull(fileFound)
        assertEquals(
            File(
                rootDir,
                "some/path/nested.txt"
            ).absolutePath,
            fileFound?.absolutePath
        )
    }

    @Test
    fun `should find existing deep nested path`() {
        val fileFound = rootDir.find("some/path")
        assertNotNull(fileFound)
        assertEquals(
            File(
                rootDir,
                "some/path"
            ).absolutePath,
            fileFound?.absolutePath
        )
    }

    @Test
    fun `should return null for non-existing absolute path`() {
        val file = File(
            rootDir,
            "no_such_file.txt"
        )
        val notFound = rootDir.find(file.absolutePath)
        assertNull(notFound)
    }

    @Test
    fun `should return null for non-existing relative path`() {
        val notFound = rootDir.find("some/path/no_such_file.txt")
        assertNull(notFound)
    }

    @Test
    fun `should return null when searching empty directory`() {
        val emptyDir = rootDir.getFile(
            "empty",
            "dir"
        )
        val notFound = emptyDir.find("no_such_file.txt")
        assertNull(notFound)
    }

    @Test
    fun `should return first match if there are several files with the same name`() {
        val fileFound = rootDir.find("some/nested.txt")
        assertNotNull(fileFound)
        assertEquals(
            File(
                rootDir,
                "some/nested.txt"
            ).absolutePath,
            fileFound?.absolutePath
        )
    }

    private fun prepareTemporaryDirectoryStructure(): File {
        // creates a temporary directory structure
        return createTempDirectory("tests_").toFile()
            .also {
                // creating files and directories
                File(
                    it,
                    "file1.json"
                ).writeText("{\"key\":\"some value\"}")
                File(
                    it,
                    "file2.log"
                ).writeText("some log file")
                it.getFile(
                    "some",
                    "nested.txt"
                )
                    .writeText("nested file")
                it.getFile(
                    "some",
                    "file3.txt"
                )
                    .writeText("nested file #3")
                it.getFile(
                    "some",
                    "path",
                    "nested.txt"
                )
                    .writeText("nested file")
            }
    }
}