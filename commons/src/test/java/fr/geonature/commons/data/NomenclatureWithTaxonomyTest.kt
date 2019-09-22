package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.NomenclatureWithTaxonomy.Companion.fromCursor
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [NomenclatureWithTaxonomy].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class NomenclatureWithTaxonomyTest {

    @Test
    fun testEquals() {
        assertEquals(NomenclatureWithTaxonomy(2,
                                              "SN",
                                              "1234:002",
                                              "label",
                                              1234),
                     NomenclatureWithTaxonomy(2,
                                              "SN",
                                              "1234:002",
                                              "label",
                                              1234))

        assertEquals(NomenclatureWithTaxonomy(2,
                                              "SN",
                                              "1234:002",
                                              "label",
                                              1234,
                                              Taxonomy("Animalia",
                                                       "Ascidies")),
                     NomenclatureWithTaxonomy(2,
                                              "SN",
                                              "1234:002",
                                              "label",
                                              1234,
                                              Taxonomy("Animalia",
                                                       "Ascidies")))

        assertEquals(NomenclatureWithTaxonomy(Nomenclature(2,
                                                           "SN",
                                                           "1234:002",
                                                           "label",
                                                           1234)),
                     NomenclatureWithTaxonomy(Nomenclature(2,
                                                           "SN",
                                                           "1234:002",
                                                           "label",
                                                           1234)))
    }

    @Test
    fun testCreateFromCompleteCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_CODE)).thenReturn(1)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_HIERARCHY)).thenReturn(2)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_DEFAULT_LABEL)).thenReturn(3)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_TYPE_ID)).thenReturn(4)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_KINGDOM)).thenReturn(5)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_GROUP)).thenReturn(6)
        `when`(cursor.getLong(0)).thenReturn(2)
        `when`(cursor.getString(1)).thenReturn("SN")
        `when`(cursor.getString(2)).thenReturn("1234:002")
        `when`(cursor.getString(3)).thenReturn("label")
        `when`(cursor.getLong(4)).thenReturn(1234)
        `when`(cursor.getString(5)).thenReturn("Animalia")
        `when`(cursor.getString(6)).thenReturn("Ascidies")

        // when getting a nomenclature with taxonomy instance from Cursor
        val nomenclatureWithTaxonomy = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureWithTaxonomy)
        assertEquals(NomenclatureWithTaxonomy(2,
                                              "SN",
                                              "1234:002",
                                              "label",
                                              1234,
                                              Taxonomy("Animalia",
                                                       "Ascidies")),
                     nomenclatureWithTaxonomy)
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_CODE)).thenReturn(1)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_HIERARCHY)).thenReturn(2)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_DEFAULT_LABEL)).thenReturn(3)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_TYPE_ID)).thenReturn(4)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_KINGDOM)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_GROUP)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getLong(0)).thenReturn(2)
        `when`(cursor.getString(1)).thenReturn("SN")
        `when`(cursor.getString(2)).thenReturn("1234:002")
        `when`(cursor.getString(3)).thenReturn("label")
        `when`(cursor.getLong(4)).thenReturn(1234)

        // when getting a nomenclature with taxonomy instance from Cursor
        val nomenclatureWithTaxonomy = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureWithTaxonomy)
        assertEquals(NomenclatureWithTaxonomy(2,
                                              "SN",
                                              "1234:002",
                                              "label",
                                              1234),
                     nomenclatureWithTaxonomy)
    }

    @Test
    fun testCreateFromInvalidCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_ID)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_CODE)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_HIERARCHY)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_DEFAULT_LABEL)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(Nomenclature.COLUMN_TYPE_ID)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_KINGDOM)).thenThrow(IllegalArgumentException::class.java)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_GROUP)).thenThrow(IllegalArgumentException::class.java)

        // when getting a nomenclature with taxonomy instance from Cursor
        val nomenclatureWithTaxonomy = fromCursor(cursor)

        // then
        assertNull(nomenclatureWithTaxonomy)
    }

    @Test
    fun testParcelable() {
        // given a nomenclature instance
        val nomenclatureWithTaxonomy = NomenclatureWithTaxonomy(2,
                                                                "SN",
                                                                "1234:002",
                                                                "label",
                                                                1234,
                                                                Taxonomy("Animalia",
                                                                         "Ascidies"))

        // when we obtain a Parcel object to write the nomenclature with taxonomy instance to it
        val parcel = Parcel.obtain()
        nomenclatureWithTaxonomy.writeToParcel(parcel,
                                               0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(nomenclatureWithTaxonomy,
                     NomenclatureWithTaxonomy.CREATOR.createFromParcel(parcel))
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(arrayOf(NomenclatureType.COLUMN_MNEMONIC,
                                  Nomenclature.COLUMN_ID,
                                  Nomenclature.COLUMN_CODE,
                                  Nomenclature.COLUMN_HIERARCHY,
                                  Nomenclature.COLUMN_DEFAULT_LABEL,
                                  Nomenclature.COLUMN_TYPE_ID,
                                  Taxonomy.COLUMN_KINGDOM,
                                  Taxonomy.COLUMN_GROUP),
                          NomenclatureWithTaxonomy.DEFAULT_PROJECTION)
    }
}