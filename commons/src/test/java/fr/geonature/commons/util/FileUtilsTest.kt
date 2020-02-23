package fr.geonature.commons.util

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import fr.geonature.mountpoint.util.FileUtils
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
    fun testGetInputsFolder() {
        assertTrue(FileUtils.getInputsFolder(application).absolutePath.contains("/Android/data/fr.geonature.commons.test/inputs"))
        assertTrue(
            FileUtils.getInputsFolder(
                application,
                "fr.geonature.sync"
            ).absolutePath.contains("/Android/data/fr.geonature.sync/inputs")
        )
    }
}
