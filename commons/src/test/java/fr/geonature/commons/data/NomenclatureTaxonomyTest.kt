package fr.geonature.commons.data

import android.database.Cursor
import fr.geonature.commons.data.NomenclatureTaxonomy.Companion.defaultProjection
import fr.geonature.commons.data.NomenclatureTaxonomy.Companion.fromCursor
import org.junit.Assert.assertArrayEquals
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

        defaultProjection().forEachIndexed { index, c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
        }

        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("Animalia")
        `when`(cursor.getString(2)).thenReturn("Ascidies")

        // when getting a NomenclatureTaxonomy instance from Cursor
        val nomenclatureTaxonomy = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureTaxonomy)
        assertEquals(NomenclatureTaxonomy(1234,
                                          Taxonomy("Animalia",
                                                   "Ascidies")),
                     nomenclatureTaxonomy)
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(arrayOf(Pair("${NomenclatureTaxonomy.TABLE_NAME}.${NomenclatureTaxonomy.COLUMN_NOMENCLATURE_ID}",
                                       "${NomenclatureTaxonomy.TABLE_NAME}_${NomenclatureTaxonomy.COLUMN_NOMENCLATURE_ID}"),
                                  Pair("${NomenclatureTaxonomy.TABLE_NAME}.${Taxonomy.COLUMN_KINGDOM}",
                                       "${NomenclatureTaxonomy.TABLE_NAME}_${Taxonomy.COLUMN_KINGDOM}"),
                                  Pair("${NomenclatureTaxonomy.TABLE_NAME}.${Taxonomy.COLUMN_GROUP}",
                                       "${NomenclatureTaxonomy.TABLE_NAME}_${Taxonomy.COLUMN_GROUP}")),
                          defaultProjection())
    }
}