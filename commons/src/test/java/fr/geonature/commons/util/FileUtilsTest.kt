package fr.geonature.commons.util

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.util.FileUtils.getInputsFolder
import fr.geonature.commons.util.FileUtils.getRelativeSharedPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit test for [FileUtils].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class FileUtilsTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testGetRelativeSharedPath() {
        assertEquals("Android/data/fr.geonature.sync/",
                     getRelativeSharedPath("fr.geonature.sync"))

        assertEquals("Android/data/fr.geonature.commons.test/",
                     getRelativeSharedPath(application.packageName))
    }

    @Test
    fun testGetInputsFolder() {
        assertTrue(getInputsFolder(application).absolutePath.contains("/Android/data/fr.geonature.commons.test/inputs"))
        assertTrue(getInputsFolder(application,
                                   "fr.geonature.sync").absolutePath.contains("/Android/data/fr.geonature.sync/inputs"))
    }
}