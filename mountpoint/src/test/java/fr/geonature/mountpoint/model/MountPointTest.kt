package fr.geonature.mountpoint.model

import android.app.Application
import android.os.Parcel
import androidx.test.core.app.ApplicationProvider
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Unit tests about [MountPoint].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class MountPointTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `should compare with another MountPoint`() {
        // given two MountPoint of same type
        val mountPoint1 = MountPoint(
            File(application.filesDir, "f1/").apply { mkdirs() },
            MountPoint.StorageType.INTERNAL
        )
        val mountPoint2 = MountPoint(
            File(application.filesDir, "f2/").apply { mkdirs() },
            MountPoint.StorageType.INTERNAL
        )

        // then
        assertTrue(mountPoint1 < mountPoint2)
        assertTrue(mountPoint2 > mountPoint1)

        // given an identical MountPoint of same type
        val mountPoint2a = MountPoint(
            File(application.filesDir, "f2/").apply { mkdirs() },
            MountPoint.StorageType.INTERNAL
        )

        assertEquals(
            0,
            mountPoint2
                .compareTo(mountPoint2a)
                .toLong()
        )

        // given another MountPoint of different type
        val mountPoint3 = MountPoint(
            File(application.filesDir, "f1/").apply { mkdirs() },
            MountPoint.StorageType.EXTERNAL
        )

        // then
        assertTrue(mountPoint1 < mountPoint3)
        assertTrue(mountPoint3 > mountPoint1)

        // given another MountPoint of different type
        val mountPoint4 = MountPoint(
            File(application.filesDir, "f1/").apply { mkdirs() },
            MountPoint.StorageType.USB
        )

        // then
        assertTrue(mountPoint1 < mountPoint4)
        assertTrue(mountPoint4 > mountPoint1)
        assertTrue(mountPoint3 < mountPoint4)
        assertTrue(mountPoint4 > mountPoint3)
    }

    @Test
    fun `should create MountPoint from Parcelable`() {
        // given MountPoint
        val mountPoint = MountPoint(
            File(application.filesDir, "f1/").apply { mkdirs() },
            MountPoint.StorageType.INTERNAL
        )

        // when we obtain a Parcel object to write the MountPoint instance to it
        val parcel = Parcel.obtain()
        mountPoint.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            mountPoint,
            parcelableCreator<MountPoint>().createFromParcel(parcel)
        )
    }
}
