package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.Taxon.Companion.fromCursor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [Taxon].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class TaxonTest {

    @Test
    fun testEquals() {
        assertEquals(Taxon(1234,
                           "taxon_01",
                           "desc",
                           true),
                     Taxon(1234,
                           "taxon_01",
                           "desc",
                           true))

        assertEquals(Taxon(1234,
                           "taxon_01",
                           "desc"),
                     Taxon(1234,
                           "taxon_01",
                           "desc"))

        assertEquals(Taxon(1234,
                           "taxon_01",
                           null),
                     Taxon(1234,
                           "taxon_01",
                           null))
    }

    @Test
    fun testCreateFromCompleteCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_NAME)).thenReturn(1)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_DESCRIPTION)).thenReturn(2)
        `when`(cursor.getColumnIndex(AbstractTaxon.COLUMN_HERITAGE)).thenReturn(3)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("desc")
        `when`(cursor.getString(3)).thenReturn("True")

        // when getting a Taxon instance from Cursor
        val taxon = fromCursor(cursor)

        // then
        assertNotNull(taxon)
        assertEquals(Taxon(1234,
                           "taxon_01",
                           "desc",
                           true),
                     taxon)
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_NAME)).thenReturn(1)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_DESCRIPTION)).thenReturn(-1)
        `when`(cursor.getColumnIndex(AbstractTaxon.COLUMN_HERITAGE)).thenReturn(-1)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn(null)
        `when`(cursor.getString(3)).thenReturn(null)

        // when getting a Taxon instance from Cursor
        val taxon = fromCursor(cursor)

        // then
        assertNotNull(taxon)
        assertEquals(Taxon(1234,
                           "taxon_01",
                           null,
                           false),
                     taxon)
    }

    @Test
    fun testCreateFromClosedCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.isClosed).thenReturn(true)

        // when getting a Taxon instance from Cursor
        val taxon = fromCursor(cursor)

        // then
        assertNull(taxon)
    }

    @Test
    fun testCreateFromInvalidCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_ID)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_NAME)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_DESCRIPTION)).thenReturn(-1)
        `when`(cursor.getColumnIndex(AbstractTaxon.COLUMN_HERITAGE)).thenReturn(-1)
        `when`(cursor.getLong(0)).thenReturn(0)
        `when`(cursor.getString(1)).thenReturn(null)
        `when`(cursor.getString(2)).thenReturn(null)
        `when`(cursor.getString(3)).thenReturn(null)

        // when getting a Taxon instance from Cursor
        val taxon = fromCursor(cursor)

        // then
        assertNull(taxon)
    }

    @Test
    fun testParcelable() {
        // given a Taxon
        val taxon = Taxon(1234,
                          "taxon_01",
                          "desc",
                          true)

        // when we obtain a Parcel object to write the Taxon instance to it
        val parcel = Parcel.obtain()
        taxon.writeToParcel(parcel,
                            0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(taxon,
                     Taxon.CREATOR.createFromParcel(parcel))
    }
}