package fr.geonature.commons.data

import android.database.Cursor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [NomenclatureTaxonomy].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class NomenclatureTaxonomyTest {

    @Test
    fun testEquals() {
        assertEquals(NomenclatureTaxonomy(1234,
                                          Taxonomy("Animalia",
                                                   "Ascidies")),
                     NomenclatureTaxonomy(1234,
                                          Taxonomy("Animalia",
                                                   "Ascidies")))
    }

    @Test
    fun testCreateFromCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(NomenclatureTaxonomy.COLUMN_NOMENCLATURE_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_KINGDOM)).thenReturn(1)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_GROUP)).thenReturn(2)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("Animalia")
        `when`(cursor.getString(2)).thenReturn("Ascidies")

        // when getting a NomenclatureTaxonomy instance from Cursor
        val nomenclatureTaxonomy = NomenclatureTaxonomy.fromCursor(cursor)

        // then
        assertNotNull(nomenclatureTaxonomy)
        assertEquals(NomenclatureTaxonomy(1234,
                                          Taxonomy("Animalia",
                                                   "Ascidies")),
                     nomenclatureTaxonomy)
    }
}