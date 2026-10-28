package fr.geonature.commons.util

import android.content.Context
import android.os.Environment
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit test for `ContextHelper`.
 *
 * @author S. Grimault
 */
class ContextHelperTest {

    private val primaryExternalStorage = File("/storage/emulated/0")
    private val internalFilesDir = File("/data/data/fr.geonature.sync/files")

    @Before
    fun setUp() {
        mockkStatic(Environment::class)
        every { Environment.getExternalStorageDirectory() } returns primaryExternalStorage
        // default: external storage mounted
        every { Environment.getExternalStorageState() } returns Environment.MEDIA_MOUNTED
        every { Environment.getExternalStorageState(any()) } returns Environment.MEDIA_MOUNTED
    }

    @After
    fun tearDown() {
        unmockkStatic(Environment::class)
    }

    @Test
    fun `should return external storage from current context if getExternalFilesDir is mounted`() {
        val externalFilesDir = File("/storage/emulated/0/Android/data/fr.geonature.sync/files")
        val context = buildMockContext(externalFilesDir)

        val appDirectory = context.getPrimaryExternalStorage()

        assertEquals(
            externalFilesDir.parentFile,
            appDirectory
        )
    }

    @Test
    fun `should fall back to getExternalStorageDirectory when getExternalFilesDir returns null but primary storage is mounted`() {
        every { Environment.getExternalStorageState() } returns Environment.MEDIA_MOUNTED
        val context = buildMockContext(externalFilesDir = null)

        val appDirectory = context.getPrimaryExternalStorage()

        assertEquals(
            primaryExternalStorage.getFile("Android", "data", context.packageName),
            appDirectory
        )
    }

    @Test
    fun `should fall back to filesDir when getExternalFilesDir mount point is not mounted`() {
        val externalFilesDir = File("/storage/emulated/0/Android/data/fr.geonature.sync/files")
        every { Environment.getExternalStorageState(externalFilesDir) } returns Environment.MEDIA_UNMOUNTED
        every { Environment.getExternalStorageState() } returns Environment.MEDIA_UNMOUNTED
        val context = buildMockContext(externalFilesDir)

        val appDirectory = context.getPrimaryExternalStorage()

        assertEquals(
            internalFilesDir.parentFile,
            appDirectory
        )
    }

    @Test
    fun `should fall back to filesDir when getExternalFilesDir is null and primary external storage is not mounted`() {
        every { Environment.getExternalStorageState() } returns Environment.MEDIA_UNMOUNTED
        val context = buildMockContext(externalFilesDir = null)

        val appDirectory = context.getPrimaryExternalStorage()

        assertEquals(
            internalFilesDir.parentFile,
            appDirectory
        )
    }

    /**
     * Builds a mock [Context] whose [Context.getExternalFilesDir] and [Context.filesDir]
     * return the given values.
     */
    private fun buildMockContext(
        externalFilesDir: File?,
        filesDir: File = internalFilesDir,
    ): Context = mockk {
        every { packageName } returns "fr.geonature.sync"
        every { getExternalFilesDir(null) } returns externalFilesDir
        every { this@mockk.filesDir } returns filesDir
    }
}