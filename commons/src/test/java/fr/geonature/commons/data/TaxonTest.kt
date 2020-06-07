package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.Taxon.Companion.defaultProjection
import fr.geonature.commons.data.Taxon.Companion.fromCursor
import fr.geonature.commons.data.helper.EntityHelper.column
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        assertEquals(
            Taxon(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                "desc",
                true
            ),
            Taxon(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                "desc",
                true
            )
        )

        assertEquals(
            Taxon(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                "desc"
            ),
            Taxon(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                "desc"
            )
        )

        assertEquals(
            Taxon(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            ),
            Taxon(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            )
        )
    }

    @Test
    fun testCreateFromCompleteCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
            `when`(cursor.getColumnIndex(c.second)).thenReturn(index)
        }

        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("Animalia")
        `when`(cursor.getString(3)).thenReturn("Ascidies")
        `when`(cursor.getString(4)).thenReturn("desc")
        `when`(cursor.getInt(5)).thenReturn(1)

        // when getting a Taxon instance from Cursor
        val taxon = fromCursor(cursor)

        // then
        assertNotNull(taxon)
        assertEquals(
            Taxon(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                "desc",
                true
            ),
            taxon
        )
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in arrayOf(
                    column(
                        AbstractTaxon.COLUMN_DESCRIPTION,
                        Taxon.TABLE_NAME
                    ),
                    column(
                        AbstractTaxon.COLUMN_HERITAGE,
                        Taxon.TABLE_NAME
                    )
                ) -> {
                    `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(-1)
                    `when`(cursor.getColumnIndex(c.second)).thenReturn(-1)
                }
                else -> {
                    `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
                }
            }
        }

        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("Animalia")
        `when`(cursor.getString(3)).thenReturn("Ascidies")

        // when getting a Taxon instance from Cursor
        val taxon = fromCursor(cursor)

        // then
        assertNotNull(taxon)
        assertEquals(
            Taxon(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            ),
            taxon
        )
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

        defaultProjection().forEach { c ->
            when (c) {
                in arrayOf(
                    column(
                        AbstractTaxon.COLUMN_DESCRIPTION,
                        Taxon.TABLE_NAME
                    ),
                    column(
                        AbstractTaxon.COLUMN_HERITAGE,
                        Taxon.TABLE_NAME
                    )
                ) -> {
                    `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(-1)
                    `when`(cursor.getColumnIndex(c.second)).thenReturn(-1)
                }
                else -> {
                    `when`(cursor.getColumnIndexOrThrow(c.second)).thenThrow(
                        IllegalArgumentException::class.java
                    )
                }
            }
        }

        // when getting a Taxon instance from Cursor
        val taxon = fromCursor(cursor)

        // then
        assertNull(taxon)
    }

    @Test
    fun testParcelable() {
        // given a Taxon
        val taxon = Taxon(
            1234,
            "taxon_01",
            Taxonomy(
                "Animalia",
                "Ascidies"
            ),
            "desc",
            true
        )

        // when we obtain a Parcel object to write the Taxon instance to it
        val parcel = Parcel.obtain()
        taxon.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            taxon,
            Taxon.CREATOR.createFromParcel(parcel)
        )
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(
            arrayOf(
                Pair(
                    "${Taxon.TABLE_NAME}.\"${AbstractTaxon.COLUMN_ID}\"",
                    "${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_ID}"
                ),
                Pair(
                    "${Taxon.TABLE_NAME}.\"${AbstractTaxon.COLUMN_NAME}\"",
                    "${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME}"
                ),
                Pair(
                    "${Taxon.TABLE_NAME}.\"${Taxonomy.COLUMN_KINGDOM}\"",
                    "${Taxon.TABLE_NAME}_${Taxonomy.COLUMN_KINGDOM}"
                ),
                Pair(
                    "${Taxon.TABLE_NAME}.\"${Taxonomy.COLUMN_GROUP}\"",
                    "${Taxon.TABLE_NAME}_${Taxonomy.COLUMN_GROUP}"
                ),
                Pair(
                    "${Taxon.TABLE_NAME}.\"${AbstractTaxon.COLUMN_DESCRIPTION}\"",
                    "${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_DESCRIPTION}"
                ),
                Pair(
                    "${Taxon.TABLE_NAME}.\"${AbstractTaxon.COLUMN_HERITAGE}\"",
                    "${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_HERITAGE}"
                )
            ),
            defaultProjection()
        )
    }

    @Test
    fun testFilter() {
        val taxonFilterByNameAndTaxonomy =
            Taxon.Filter()
                .byNameOrDescription("as")
                .byTaxonomy(
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    )
                )
                .build()

        assertEquals(
            "(${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME} LIKE ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_DESCRIPTION} LIKE ?) AND ((${Taxon.TABLE_NAME}_${Taxonomy.COLUMN_KINGDOM} = ?) AND (${Taxon.TABLE_NAME}_${Taxonomy.COLUMN_GROUP} = ?))",
            taxonFilterByNameAndTaxonomy.first
        )
        assertArrayEquals(
            arrayOf(
                "%as%",
                "%as%",
                "Animalia",
                "Ascidies"
            ),
            taxonFilterByNameAndTaxonomy.second
        )

        val taxonFilterByNameAndKingdom =
            Taxon.Filter()
                .byNameOrDescription("as")
                .byTaxonomy(
                    Taxonomy(
                        "Animalia"
                    )
                )
                .build()

        assertEquals(
            "(${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME} LIKE ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_DESCRIPTION} LIKE ?) AND (${Taxon.TABLE_NAME}_${Taxonomy.COLUMN_KINGDOM} = ?)",
            taxonFilterByNameAndKingdom.first
        )
        assertArrayEquals(
            arrayOf(
                "%as%",
                "%as%",
                "Animalia"
            ),
            taxonFilterByNameAndKingdom.second
        )

        val taxonFilterByKingdom = Taxon.Filter()
            .byKingdom("Animalia")
            .build()

        assertEquals(
            "(${Taxon.TABLE_NAME}_${Taxonomy.COLUMN_KINGDOM} = ?)",
            taxonFilterByKingdom.first
        )
        assertArrayEquals(
            arrayOf("Animalia"),
            taxonFilterByKingdom.second
        )

        val taxonFilterByAnyTaxonomy = Taxon.Filter()
            .byTaxonomy(Taxonomy(""))
            .build()

        assertEquals(
            "",
            taxonFilterByAnyTaxonomy.first
        )
        assertTrue(taxonFilterByAnyTaxonomy.second.isEmpty())
    }
}
