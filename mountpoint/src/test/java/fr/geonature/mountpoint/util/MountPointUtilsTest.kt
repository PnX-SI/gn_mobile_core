package fr.geonature.mountpoint.util

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.mountpoint.model.MountPoint.StorageType.INTERNAL
import fr.geonature.mountpoint.util.MountPointUtils.formatStorageSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [MountPointUtils].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class MountPointUtilsTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = getApplicationContext()
    }

    @Test
    fun testGetInternalStorage() {
        val mountPoint = MountPointUtils.getInternalStorage(application)

        assertNotNull(mountPoint)
        assertEquals(
            INTERNAL,
            mountPoint.storageType
        )
    }

    @Test
    fun testFormatStorageSize() {
        val storageInB = 128L
        val storageInBFormatted = formatStorageSize(
            application,
            storageInB
        )
        assertNotNull(storageInB)
        assertEquals(
            "128 B",
            storageInBFormatted
        )

        val storageInKb = 1024L
        val storageInKbFormatted = formatStorageSize(
            application,
            storageInKb
        )

        assertNotNull(storageInKb)
        assertEquals(
            "1.0 kB",
            storageInKbFormatted
        )

        val storageInMb = storageInKb * 1024
        val storageInMbFormatted = formatStorageSize(
            application,
            storageInMb
        )

        assertNotNull(storageInMb)
        assertEquals(
            "1.0 MB",
            storageInMbFormatted
        )

        val storageInGb = storageInMb * 1024
        val storageInGbFormatted = formatStorageSize(
            application,
            storageInGb
        )

        assertNotNull(storageInGb)
        assertEquals(
            "1.0 GB",
            storageInGbFormatted
        )
    }
}
