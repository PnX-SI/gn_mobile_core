package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.Taxon.Companion.fromCursor
import org.junit.Assert
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
                           Taxonomy("Animalia",
                                    "Ascidies"),
                           "desc",
                           true),
                     Taxon(1234,
                           "taxon_01",
                           Taxonomy("Animalia",
                                    "Ascidies"),
                           "desc",
                           true))

        assertEquals(Taxon(1234,
                           "taxon_01",
                           Taxonomy("Animalia",
                                    "Ascidies"),
                           "desc"),
                     Taxon(1234,
                           "taxon_01",
                           Taxonomy("Animalia",
                                    "Ascidies"),
                           "desc"))

        assertEquals(Taxon(1234,
                           "taxon_01",
                           Taxonomy("Animalia",
                                    "Ascidies"),
                           null),
                     Taxon(1234,
                           "taxon_01",
                           Taxonomy("Animalia",
                                    "Ascidies"),
                           null))
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
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("Animalia")
        `when`(cursor.getString(3)).thenReturn("Ascidies")
        `when`(cursor.getString(4)).thenReturn("desc")
        `when`(cursor.getString(5)).thenReturn("True")

        // when getting a Taxon instance from Cursor
        val taxon = fromCursor(cursor)

        // then
        assertNotNull(taxon)
        assertEquals(Taxon(1234,
                           "taxon_01",
                           Taxonomy("Animalia",
                                    "Ascidies"),
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
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_KINGDOM)).thenReturn(2)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_GROUP)).thenReturn(3)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_DESCRIPTION)).thenReturn(-1)
        `when`(cursor.getColumnIndex(AbstractTaxon.COLUMN_HERITAGE)).thenReturn(-1)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("Animalia")
        `when`(cursor.getString(3)).thenReturn("Ascidies")

        // when getting a Taxon instance from Cursor
        val taxon = fromCursor(cursor)

        // then
        assertNotNull(taxon)
        assertEquals(Taxon(1234,
                           "taxon_01",
                           Taxonomy("Animalia",
                                    "Ascidies"),
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
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_KINGDOM)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_GROUP)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(AbstractTaxon.COLUMN_DESCRIPTION)).thenReturn(-1)
        `when`(cursor.getColumnIndex(AbstractTaxon.COLUMN_HERITAGE)).thenReturn(-1)

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
                          Taxonomy("Animalia",
                                   "Ascidies"),
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

    @Test
    fun testDefaultProjection() {
        Assert.assertArrayEquals(arrayOf(AbstractTaxon.COLUMN_ID,
                                         AbstractTaxon.COLUMN_NAME,
                                         Taxonomy.COLUMN_KINGDOM,
                                         Taxonomy.COLUMN_GROUP,
                                         AbstractTaxon.COLUMN_DESCRIPTION,
                                         AbstractTaxon.COLUMN_HERITAGE),
                                 AbstractTaxon.DEFAULT_PROJECTION)
    }
}