package fr.geonature.commons.data.entity

import android.database.Cursor
import fr.geonature.commons.data.entity.NomenclatureTaxonomy.Companion.defaultProjection
import fr.geonature.commons.data.entity.NomenclatureTaxonomy.Companion.fromCursor
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [NomenclatureTaxonomy].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class NomenclatureTaxonomyTest {

    @MockK
    private lateinit var cursor: Cursor

    @Before
    fun setUp() {
        init(this)

        every { cursor.isClosed } returns false
    }

    @Test
    fun testEquals() {
        assertEquals(
            NomenclatureTaxonomy(
                1234,
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            ),
            NomenclatureTaxonomy(
                1234,
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            )
        )
    }

    @Test
    fun testCreateFromCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            every { cursor.getColumnIndexOrThrow(c.second) } returns index
        }
        every { cursor.getLong(0) } returns 1234
        every { cursor.isNull(0) } returns false
        every { cursor.getString(1) } returns "Animalia"
        every { cursor.getString(2) } returns "Ascidies"

        // when getting a NomenclatureTaxonomy instance from Cursor
        val nomenclatureTaxonomy = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureTaxonomy)
        assertEquals(
            NomenclatureTaxonomy(
                1234,
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            ),
            nomenclatureTaxonomy
        )
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(
            arrayOf(
                Pair(
                    "${NomenclatureTaxonomy.TABLE_NAME}.\"${NomenclatureTaxonomy.COLUMN_NOMENCLATURE_ID}\"",
                    "${NomenclatureTaxonomy.TABLE_NAME}_${NomenclatureTaxonomy.COLUMN_NOMENCLATURE_ID}"
                ),
                Pair(
                    "${NomenclatureTaxonomy.TABLE_NAME}.\"${Taxonomy.COLUMN_KINGDOM}\"",
                    "${NomenclatureTaxonomy.TABLE_NAME}_${Taxonomy.COLUMN_KINGDOM}"
                ),
                Pair(
                    "${NomenclatureTaxonomy.TABLE_NAME}.\"${Taxonomy.COLUMN_GROUP}\"",
                    "${NomenclatureTaxonomy.TABLE_NAME}_${Taxonomy.COLUMN_GROUP}"
                )
            ),
            defaultProjection()
        )
    }
}
