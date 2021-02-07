package fr.geonature.mountpoint.model

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [MountPoint].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class MountPointTest {

    @Test
    fun testCompareTo() {
        // given two MountPoint of same type
        val mountPoint1 = MountPoint(
            "/storage1",
            MountPoint.StorageType.INTERNAL
        )
        val mountPoint2 = MountPoint(
            "/storage2",
            MountPoint.StorageType.INTERNAL
        )

        // then
        assertTrue(mountPoint1 < mountPoint2)
        assertTrue(mountPoint2 > mountPoint1)

        // given an identical MountPoint of same type
        val mountPoint2a = MountPoint(
            "/storage2",
            MountPoint.StorageType.INTERNAL
        )

        assertEquals(
            0,
            mountPoint2.compareTo(mountPoint2a).toLong()
        )

        // given another MountPoint of different type
        val mountPoint3 = MountPoint(
            "/mnt/sdcard1",
            MountPoint.StorageType.EXTERNAL
        )

        // then
        assertTrue(mountPoint1 < mountPoint3)
        assertTrue(mountPoint3 > mountPoint1)

        // given another MountPoint of different type
        val mountPoint4 = MountPoint(
            "/another",
            MountPoint.StorageType.USB
        )

        // then
        assertTrue(mountPoint1 < mountPoint4)
        assertTrue(mountPoint4 > mountPoint1)
        assertTrue(mountPoint3 < mountPoint4)
        assertTrue(mountPoint4 > mountPoint3)
    }

    @Test
    fun testParcelable() {
        // given MountPoint
        val mountPoint = MountPoint(
            "/storage1",
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
            MountPoint.CREATOR.createFromParcel(parcel)
        )
    }
}
