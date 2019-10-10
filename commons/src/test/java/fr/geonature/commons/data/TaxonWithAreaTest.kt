package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.TaxonWithArea.Companion.fromCursor
import org.junit.Assert.assertArrayEquals
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
                                   Taxonomy("Animalia",
                                            "Ascidies"),
                                   "desc",
                                   true,
                                   null),
                     TaxonWithArea(1234,
                                   "taxon_01",
                                   Taxonomy("Animalia",
                                            "Ascidies"),
                                   "desc",
                                   true,
                                   null))

        assertEquals(TaxonWithArea(1234,
                                   "taxon_01",
                                   Taxonomy("Animalia",
                                            "Ascidies"),
                                   "desc",
                                   true,
                                   TaxonArea(1234,
                                             10,
                                             "red",
                                             3,
                                             now)),
                     TaxonWithArea(1234,
                                   "taxon_01",
                                   Taxonomy("Animalia",
                                            "Ascidies"),
                                   "desc",
                                   true,
                                   TaxonArea(1234,
                                             10,
                                             "red",
                                             3,
                                             now)))

        assertEquals(TaxonWithArea(Taxon(1234,
                                         "taxon_01",
                                         Taxonomy("Animalia",
                                                  "Ascidies"),
                                         "desc")),
                     TaxonWithArea(Taxon(1234,
                                         "taxon_01",
                                         Taxonomy("Animalia",
                                                  "Ascidies"),
                                         "desc")))
    }

    @Test
    fun testCreateFromCompleteCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_NAME)).thenReturn(1)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_KINGDOM)).thenReturn(2)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_GROUP)).thenReturn(3)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_DESCRIPTION)).thenReturn(4)
        `when`(cursor.getColumnIndex(AbstractTaxon.COLUMN_HERITAGE)).thenReturn(5)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_TAXON_ID)).thenReturn(6)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_AREA_ID)).thenReturn(7)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_COLOR)).thenReturn(8)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_NUMBER_OF_OBSERVERS)).thenReturn(9)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_LAST_UPDATED_AT)).thenReturn(10)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("Animalia")
        `when`(cursor.getString(3)).thenReturn("Ascidies")
        `when`(cursor.getString(4)).thenReturn("desc")
        `when`(cursor.getString(5)).thenReturn("True")
        `when`(cursor.getLong(6)).thenReturn(1234)
        `when`(cursor.getLong(7)).thenReturn(10)
        `when`(cursor.getString(8)).thenReturn("red")
        `when`(cursor.getInt(9)).thenReturn(3)
        `when`(cursor.getLong(10)).thenReturn(1477642500000)

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = fromCursor(cursor)

        // then
        assertNotNull(taxonWithArea)
        assertEquals(TaxonWithArea(1234,
                                   "taxon_01",
                                   Taxonomy("Animalia",
                                            "Ascidies"),
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
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_NAME)).thenReturn(1)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_KINGDOM)).thenReturn(2)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_GROUP)).thenReturn(3)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_DESCRIPTION)).thenReturn(4)
        `when`(cursor.getColumnIndex(AbstractTaxon.COLUMN_HERITAGE)).thenReturn(5)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_TAXON_ID)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_AREA_ID)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_COLOR)).thenReturn(-1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_NUMBER_OF_OBSERVERS)).thenReturn(-1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_LAST_UPDATED_AT)).thenReturn(-1)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("Animalia")
        `when`(cursor.getString(3)).thenReturn("Ascidies")
        `when`(cursor.getString(4)).thenReturn("desc")
        `when`(cursor.getString(5)).thenReturn("True")

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = fromCursor(cursor)

        // then
        assertNotNull(taxonWithArea)
        assertEquals(TaxonWithArea(1234,
                                   "taxon_01",
                                   Taxonomy("Animalia",
                                            "Ascidies"),
                                   "desc",
                                   true,
                                   null),
                     taxonWithArea)
    }

    @Test
    fun testCreateFromIncompleteCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_NAME)).thenReturn(1)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_KINGDOM)).thenReturn(2)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_GROUP)).thenReturn(3)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_DESCRIPTION)).thenReturn(4)
        `when`(cursor.getColumnIndex(AbstractTaxon.COLUMN_HERITAGE)).thenReturn(5)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_TAXON_ID)).thenReturn(6)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_AREA_ID)).thenReturn(7)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_COLOR)).thenReturn(-1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_NUMBER_OF_OBSERVERS)).thenReturn(-1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_LAST_UPDATED_AT)).thenReturn(-1)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("Animalia")
        `when`(cursor.getString(3)).thenReturn("Ascidies")
        `when`(cursor.getString(4)).thenReturn("desc")
        `when`(cursor.getString(5)).thenReturn("True")
        `when`(cursor.getLong(6)).thenReturn(0)
        `when`(cursor.getLong(7)).thenReturn(0)

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = fromCursor(cursor)

        // then
        assertNotNull(taxonWithArea)
        assertEquals(TaxonWithArea(1234,
                                   "taxon_01",
                                   Taxonomy("Animalia",
                                            "Ascidies"),
                                   "desc",
                                   true,
                                   null),
                     taxonWithArea)
    }

    @Test
    fun testCreateFromClosedCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.isClosed).thenReturn(true)

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = fromCursor(cursor)

        // then
        assertNull(taxonWithArea)
    }

    @Test
    fun testCreateFromInvalidCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_ID)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_NAME)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_KINGDOM)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_GROUP)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_DESCRIPTION)).thenReturn(-1)
        `when`(cursor.getColumnIndex(AbstractTaxon.COLUMN_HERITAGE)).thenReturn(-1)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_TAXON_ID)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(TaxonArea.COLUMN_AREA_ID)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_COLOR)).thenReturn(-1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_NUMBER_OF_OBSERVERS)).thenReturn(-1)
        `when`(cursor.getColumnIndex(TaxonArea.COLUMN_LAST_UPDATED_AT)).thenReturn(-1)

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = fromCursor(cursor)

        // then
        assertNull(taxonWithArea)
    }

    @Test
    fun testParcelable() {
        // given a TaxonWithArea
        val taxonWithArea = TaxonWithArea(1234,
                                          "taxon_01",
                                          Taxonomy("Animalia",
                                                   "Ascidies"),
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

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(arrayOf(AbstractTaxon.COLUMN_ID,
                                  AbstractTaxon.COLUMN_NAME,
                                  Taxonomy.COLUMN_KINGDOM,
                                  Taxonomy.COLUMN_GROUP,
                                  AbstractTaxon.COLUMN_DESCRIPTION,
                                  AbstractTaxon.COLUMN_HERITAGE,
                                  TaxonArea.COLUMN_TAXON_ID,
                                  TaxonArea.COLUMN_AREA_ID,
                                  TaxonArea.COLUMN_COLOR,
                                  TaxonArea.COLUMN_NUMBER_OF_OBSERVERS,
                                  TaxonArea.COLUMN_LAST_UPDATED_AT),
                          TaxonWithArea.DEFAULT_PROJECTION)
    }
}