package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.TaxonArea.Companion.fromCursor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.Date

/**
 * Unit tests about [TaxonArea].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class TaxonAreaTest {

    @Test
    fun testEquals() {
        val now = Date.from(Instant.now())

        assertEquals(TaxonArea(1234,
                               10,
                               "red",
                               3,
                               now),
                     TaxonArea(1234,
                               10,
                               "red",
                               3,
                               now))

        assertEquals(TaxonArea(1234,
                               10,
                               null,
                               3,
                               now),
                     TaxonArea(1234,
                               10,
                               null,
                               3,
                               now))
    }

    @Test
    fun testCreateFromCompleteCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_TAXON_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_AREA_ID)).thenReturn(1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_COLOR)).thenReturn(2)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_NUMBER_OF_OBSERVERS)).thenReturn(3)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_LAST_UPDATED_AT)).thenReturn(4)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getLong(1)).thenReturn(10)
        `when`(cursor.getString(2)).thenReturn("red")
        `when`(cursor.getInt(3)).thenReturn(3)
        `when`(cursor.getLong(4)).thenReturn(1477642500000)

        // when getting a TaxonArea instance from Cursor
        val taxonArea = fromCursor(cursor)

        // then
        assertNotNull(taxonArea)
        assertEquals(TaxonArea(1234,
                               10,
                               "red",
                               3,
                               Date.from(Instant.parse("2016-10-28T08:15:00Z"))),
                     taxonArea)
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_TAXON_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_AREA_ID)).thenReturn(1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_COLOR)).thenReturn(-1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_NUMBER_OF_OBSERVERS)).thenReturn(-1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_LAST_UPDATED_AT)).thenReturn(-1)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getLong(1)).thenReturn(10)
        `when`(cursor.getString(2)).thenReturn(null)
        `when`(cursor.getInt(3)).thenReturn(0)
        `when`(cursor.getLong(4)).thenReturn(0)

        // when getting a TaxonArea instance from Cursor
        val taxonArea = fromCursor(cursor)

        // then
        assertNotNull(taxonArea)
        assertEquals(TaxonArea(1234,
                               10,
                               "#00000000",
                               0,
                               null),
                     taxonArea)
    }

    @Test
    fun testCreateFromClosedCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.isClosed).thenReturn(true)

        // when getting a TaxonArea instance from Cursor
        val taxonArea = fromCursor(cursor)

        // then
        assertNull(taxonArea)
    }

    @Test()
    fun testCreateFromInvalidCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_TAXON_ID)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_AREA_ID)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_COLOR)).thenReturn(-1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_NUMBER_OF_OBSERVERS)).thenReturn(-1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_LAST_UPDATED_AT)).thenReturn(-1)
        `when`(cursor.getLong(0)).thenReturn(0)
        `when`(cursor.getLong(1)).thenReturn(0)
        `when`(cursor.getString(2)).thenReturn(null)
        `when`(cursor.getInt(3)).thenReturn(0)
        `when`(cursor.getLong(4)).thenReturn(0)

        // when getting a TaxonArea instance from Cursor
        val taxonArea = fromCursor(cursor)

        // then
        assertNull(taxonArea)
    }

    @Test
    fun testParcelable() {
        // given a TaxonArea
        val taxonArea = TaxonArea(1234,
                                  10,
                                  "red",
                                  3,
                                  Date.from(Instant.now()))

        // when we obtain a Parcel object to write the TaxonWithArea instance to it
        val parcel = Parcel.obtain()
        taxonArea.writeToParcel(parcel,
                                0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(taxonArea,
                     TaxonArea.CREATOR.createFromParcel(parcel))
    }
}