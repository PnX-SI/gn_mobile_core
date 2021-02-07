package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.TaxonWithArea.Companion.defaultProjection
import fr.geonature.commons.data.TaxonWithArea.Companion.fromCursor
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

        assertEquals(
            TaxonWithArea(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null,
                "desc",
                null,
                null
            ),
            TaxonWithArea(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null,
                "desc",
                null,
                null
            )
        )

        assertEquals(
            TaxonWithArea(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                "taxon_01_common",
                "desc",
                "ES - 1234",
                TaxonArea(
                    1234,
                    10,
                    "red",
                    3,
                    now
                )
            ),
            TaxonWithArea(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                "taxon_01_common",
                "desc",
                "ES - 1234",
                TaxonArea(
                    1234,
                    10,
                    "red",
                    3,
                    now
                )
            )
        )

        assertEquals(
            TaxonWithArea(
                Taxon(
                    1234,
                    "taxon_01",
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    ),
                    null,
                    "desc"
                )
            ),
            TaxonWithArea(
                Taxon(
                    1234,
                    "taxon_01",
                    Taxonomy(
                        "Animalia",
                        "Ascidies"
                    ),
                    null,
                    "desc"
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
        `when`(cursor.getString(4)).thenReturn("taxon_01_common")
        `when`(cursor.getString(5)).thenReturn("desc")
        `when`(cursor.getString(6)).thenReturn("ES - 1234")
        `when`(cursor.getLong(7)).thenReturn(1234)
        `when`(cursor.getLong(8)).thenReturn(10)
        `when`(cursor.getString(9)).thenReturn("red")
        `when`(cursor.getInt(10)).thenReturn(3)
        `when`(cursor.getLong(11)).thenReturn(1477642500000)

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = fromCursor(cursor)

        // then
        assertNotNull(taxonWithArea)
        assertEquals(
            TaxonWithArea(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                "taxon_01_common",
                "desc",
                "ES - 1234",
                TaxonArea(
                    1234,
                    10,
                    "red",
                    3,
                    Date.from(Instant.parse("2016-10-28T08:15:00Z"))
                )
            ),
            taxonWithArea
        )
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in TaxonArea.defaultProjection() -> {
                    `when`(cursor.getColumnIndexOrThrow(c.second)).thenThrow(
                        IllegalArgumentException::class.java
                    )
                    `when`(cursor.getColumnIndex(c.second)).thenReturn(-1)
                }
                else -> {
                    `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
                    `when`(cursor.getColumnIndex(c.second)).thenReturn(index)
                }
            }
        }

        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("Animalia")
        `when`(cursor.getString(3)).thenReturn("Ascidies")
        `when`(cursor.getString(4)).thenReturn(null)
        `when`(cursor.getString(5)).thenReturn("desc")
        `when`(cursor.getString(6)).thenReturn(null)

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = fromCursor(cursor)

        // then
        assertNotNull(taxonWithArea)
        assertEquals(
            TaxonWithArea(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null,
                "desc",
                null,
                null
            ),
            taxonWithArea
        )
    }

    @Test
    fun testCreateFromIncompleteCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in TaxonArea.defaultProjection() -> {
                    `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
                    `when`(cursor.getColumnIndex(c.second)).thenReturn(-1)
                }
                else -> {
                    `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
                    `when`(cursor.getColumnIndex(c.second)).thenReturn(index)
                }
            }
        }

        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("Animalia")
        `when`(cursor.getString(3)).thenReturn("Ascidies")
        `when`(cursor.getString(4)).thenReturn(null)
        `when`(cursor.getString(5)).thenReturn("desc")
        `when`(cursor.getString(6)).thenReturn(null)
        `when`(cursor.getLong(7)).thenReturn(0)
        `when`(cursor.getLong(8)).thenReturn(0)

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = fromCursor(cursor)

        // then
        assertNotNull(taxonWithArea)
        assertEquals(
            TaxonWithArea(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null,
                "desc",
                null,
                null
            ),
            taxonWithArea
        )
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

        defaultProjection().forEach { c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenThrow(IllegalArgumentException::class.java)
            `when`(cursor.getColumnIndex(c.second)).thenReturn(-1)
        }

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = fromCursor(cursor)

        // then
        assertNull(taxonWithArea)
    }

    @Test
    fun testParcelable() {
        // given a TaxonWithArea
        val taxonWithArea = TaxonWithArea(
            1234,
            "taxon_01",
            Taxonomy(
                "Animalia",
                "Ascidies"
            ),
            "taxon_01_common",
            "desc",
            "ES - 1234",
            TaxonArea(
                1234,
                10,
                "red",
                3,
                Date.from(Instant.now())
            )
        )

        // when we obtain a Parcel object to write the TaxonWithArea instance to it
        val parcel = Parcel.obtain()
        taxonWithArea.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            taxonWithArea,
            TaxonWithArea.CREATOR.createFromParcel(parcel)
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
                    "${Taxon.TABLE_NAME}.\"${AbstractTaxon.COLUMN_NAME_COMMON}\"",
                    "${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME_COMMON}"
                ),
                Pair(
                    "${Taxon.TABLE_NAME}.\"${AbstractTaxon.COLUMN_DESCRIPTION}\"",
                    "${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_DESCRIPTION}"
                ),
                Pair(
                    "${Taxon.TABLE_NAME}.\"${AbstractTaxon.COLUMN_RANK}\"",
                    "${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_RANK}"
                ),
                Pair(
                    "${TaxonArea.TABLE_NAME}.\"${TaxonArea.COLUMN_TAXON_ID}\"",
                    "${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_TAXON_ID}"
                ),
                Pair(
                    "${TaxonArea.TABLE_NAME}.\"${TaxonArea.COLUMN_AREA_ID}\"",
                    "${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_AREA_ID}"
                ),
                Pair(
                    "${TaxonArea.TABLE_NAME}.\"${TaxonArea.COLUMN_COLOR}\"",
                    "${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_COLOR}"
                ),
                Pair(
                    "${TaxonArea.TABLE_NAME}.\"${TaxonArea.COLUMN_NUMBER_OF_OBSERVERS}\"",
                    "${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_NUMBER_OF_OBSERVERS}"
                ),
                Pair(
                    "${TaxonArea.TABLE_NAME}.\"${TaxonArea.COLUMN_LAST_UPDATED_AT}\"",
                    "${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_LAST_UPDATED_AT}"
                )
            ),
            defaultProjection()
        )
    }

    @Test
    fun testFilter() {
        val taxonFilterByAreaColors =
            TaxonWithArea.Filter()
                .byAreaColors(
                    "red",
                    "grey"
                )
                .build()

        assertEquals(
            "(${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_COLOR} IN ('red', 'grey'))",
            taxonFilterByAreaColors.first
        )
        assertTrue(taxonFilterByAreaColors.second.isEmpty())

        val taxonFilterByNameAndAreaColors =
            (
                TaxonWithArea.Filter()
                    .byNameOrDescriptionOrRank("as") as TaxonWithArea.Filter
                )
                .byAreaColors(
                    "red",
                    "grey"
                )
                .build()

        assertEquals(
            "(${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME} LIKE ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME_COMMON} LIKE ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_DESCRIPTION} LIKE ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_RANK} LIKE ?) AND (${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_COLOR} IN ('red', 'grey'))",
            taxonFilterByNameAndAreaColors.first
        )
        assertArrayEquals(
            arrayOf(
                "%as%",
                "%as%",
                "%as%",
                "%as%"
            ),
            taxonFilterByNameAndAreaColors.second
        )
    }
}
