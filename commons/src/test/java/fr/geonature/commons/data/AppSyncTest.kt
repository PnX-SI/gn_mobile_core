package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.Date

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
    fun testCreateFromCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(AppSync.COLUMN_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(AppSync.COLUMN_LAST_SYNC)).thenReturn(1)
        `when`(cursor.getColumnIndex(AppSync.COLUMN_INPUTS_TO_SYNCHRONIZE)).thenReturn(2)
        `when`(cursor.getString(0)).thenReturn("fr.geonature.sync")
        `when`(cursor.getLong(1)).thenReturn(1477642500000)
        `when`(cursor.getInt(2)).thenReturn(3)

        // when getting AppSync instance from Cursor
        val appSync = AppSync.fromCursor(cursor)

        // then
        assertNotNull(appSync)
        assertEquals(
            AppSync(
                "fr.geonature.sync", Date.from(Instant.parse("2016-10-28T08:15:00Z")), 3
            ), appSync
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