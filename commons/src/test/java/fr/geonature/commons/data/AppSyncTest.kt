package fr.geonature.commons.data

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.*

/**
 * Unit tests about [AppSync].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class AppSyncTest {

    @Test
    fun testEquals() {
        val now = Date.from(Instant.now())

        assertEquals(
            AppSync("fr.geonature.sync", now, 3), AppSync("fr.geonature.sync", now, 3)
        )
    }

    @Test
    fun testParcelable() {
        // given AppSync
        val appSync = AppSync("fr.geonature.sync", Date.from(Instant.now()), 3)

        // when we obtain a Parcel object to write the AppSync instance to it
        val parcel = Parcel.obtain()
        appSync.writeToParcel(parcel, 0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(appSync, AppSync.CREATOR.createFromParcel(parcel))
    }
}