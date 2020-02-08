package fr.geonature.mountpoint.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.mountpoint.model.MountPoint.StorageType.INTERNAL
import fr.geonature.mountpoint.util.MountPointUtils.formatStorageSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [MountPointUtils].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class MountPointUtilsTest {

    @Test
    fun testGetInternalStorage() {
        val mountPoint = MountPointUtils.getInternalStorage()

        assertNotNull(mountPoint)
        assertEquals(
            INTERNAL,
            mountPoint.storageType
        )
    }

    @Test
    fun testFormatStorageSize() {
        val context: Context = getApplicationContext()

        val storageInB = 128L
        val storageInBFormatted = formatStorageSize(
            context,
            storageInB
        )
        assertNotNull(storageInB)
        assertEquals(
            "128 B",
            storageInBFormatted
        )

        val storageInKb = 1024L
        val storageInKbFormatted = formatStorageSize(
            context,
            storageInKb
        )

        assertNotNull(storageInKb)
        assertEquals(
            "1.0 kB",
            storageInKbFormatted
        )

        val storageInMb = storageInKb * 1024
        val storageInMbFormatted = formatStorageSize(
            context,
            storageInMb
        )

        assertNotNull(storageInMb)
        assertEquals(
            "1.0 MB",
            storageInMbFormatted
        )

        val storageInGb = storageInMb * 1024
        val storageInGbFormatted = formatStorageSize(
            context,
            storageInGb
        )

        assertNotNull(storageInGb)
        assertEquals(
            "1.0 GB",
            storageInGbFormatted
        )
    }
}