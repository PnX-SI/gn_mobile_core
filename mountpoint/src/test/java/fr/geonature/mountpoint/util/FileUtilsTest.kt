package fr.geonature.mountpoint.util

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import fr.geonature.mountpoint.model.MountPoint.StorageType.INTERNAL
import fr.geonature.mountpoint.util.FileUtils.getRelativeSharedPath
import fr.geonature.mountpoint.util.FileUtils.getRootFolder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit test for [FileUtils].
 *
 * @author S. Grimault
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
        assertEquals(
            "Android/data/fr.geonature.sync/",
            getRelativeSharedPath("fr.geonature.sync")
        )

        assertEquals(
            "Android/data/fr.geonature.mountpoint.test/",
            getRelativeSharedPath(application.packageName)
        )
    }

    @Test
    fun testGetRootFolder() {
        assertTrue(
            getRootFolder(
                application,
                INTERNAL
            ).absolutePath.contains("/Android/data/fr.geonature.mountpoint.test")
        )
        assertTrue(
            getRootFolder(
                application,
                INTERNAL,
                "fr.geonature.sync"
            ).absolutePath.contains("/Android/data/fr.geonature.sync")
        )
    }
}
