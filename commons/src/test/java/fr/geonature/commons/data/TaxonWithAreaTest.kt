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
 * Unit tests about [TaxonWithArea].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class TaxonWithAreaTest {

    @Test
    fun testEquals() {
        val now = Date.from(Instant.now())

        assertEquals(TaxonWithArea(1234,
                                   "taxon_01",
                                   "desc",
                                   true,
                                   null),
                     TaxonWithArea(1234,
                                   "taxon_01",
                                   "desc",
                                   true,
                                   null))

        assertEquals(TaxonWithArea(1234,
                                   "taxon_01",
                                   "desc",
                                   true,
                                   TaxonArea(1234,
                                             10,
                                             "red",
                                             3,
                                             now)),
                     TaxonWithArea(1234,
                                   "taxon_01",
                                   "desc",
                                   true,
                                   TaxonArea(1234,
                                             10,
                                             "red",
                                             3,
                                             now)))

        assertEquals(TaxonWithArea(Taxon(1234,
                                         "taxon_01",
                                         "desc")),
                     TaxonWithArea(Taxon(1234,
                                         "taxon_01",
                                         "desc")))
    }

    @Test
    fun testCreateFromCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_NAME)).thenReturn(1)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_DESCRIPTION)).thenReturn(2)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_HERITAGE)).thenReturn(3)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_TAXON_ID)).thenReturn(4)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_AREA_ID)).thenReturn(5)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_COLOR)).thenReturn(6)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_NUMBER_OF_OBSERVERS)).thenReturn(7)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_LAST_UPDATED_AT)).thenReturn(8)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("desc")
        `when`(cursor.getString(3)).thenReturn("True")
        `when`(cursor.getLong(4)).thenReturn(1234)
        `when`(cursor.getLong(5)).thenReturn(10)
        `when`(cursor.getString(6)).thenReturn("red")
        `when`(cursor.getInt(7)).thenReturn(3)
        `when`(cursor.getLong(8)).thenReturn(1477642500000)

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = TaxonWithArea.fromCursor(cursor)

        // then
        assertNotNull(taxonWithArea)
        assertEquals(TaxonWithArea(1234,
                                   "taxon_01",
                                   "desc",
                                   true,
                                   TaxonArea(1234,
                                             10,
                                             "red",
                                             3,
                                             Date.from(Instant.parse("2016-10-28T08:15:00Z")))),
                     taxonWithArea)
    }

    @Test
    fun testParcelable() {
        // given a TaxonWithArea
        val taxonWithArea = TaxonWithArea(1234,
                                          "taxon_01",
                                          "desc",
                                          true,
                                          TaxonArea(1234,
                                                    10,
                                                    "red",
                                                    3,
                                                    Date.from(Instant.now())))

        // when we obtain a Parcel object to write the TaxonWithArea instance to it
        val parcel = Parcel.obtain()
        taxonWithArea.writeToParcel(parcel,
                                    0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(taxonWithArea,
                     TaxonWithArea.CREATOR.createFromParcel(parcel))
    }
}