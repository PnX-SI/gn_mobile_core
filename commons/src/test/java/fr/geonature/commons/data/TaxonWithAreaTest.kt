package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.TaxonWithArea.Companion.defaultProjection
import fr.geonature.commons.data.TaxonWithArea.Companion.fromCursor
import java.time.Instant
import java.util.Date
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
                "desc",
                true,
                null
            ),
            TaxonWithArea(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                "desc",
                true,
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
                "desc",
                true,
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
                "desc",
                true,
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
        `when`(cursor.getString(4)).thenReturn("desc")
        `when`(cursor.getInt(5)).thenReturn(1)
        `when`(cursor.getLong(6)).thenReturn(1234)
        `when`(cursor.getLong(7)).thenReturn(10)
        `when`(cursor.getString(8)).thenReturn("red")
        `when`(cursor.getInt(9)).thenReturn(3)
        `when`(cursor.getLong(10)).thenReturn(1477642500000)

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
                "desc",
                true,
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
        `when`(cursor.getString(4)).thenReturn("desc")
        `when`(cursor.getInt(5)).thenReturn(1)

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
                "desc",
                true,
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
        `when`(cursor.getString(4)).thenReturn("desc")
        `when`(cursor.getInt(5)).thenReturn(1)
        `when`(cursor.getLong(6)).thenReturn(0)
        `when`(cursor.getLong(7)).thenReturn(0)

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
                "desc",
                true,
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
            "desc",
            true,
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
                    "${Taxon.TABLE_NAME}.\"${AbstractTaxon.COLUMN_DESCRIPTION}\"",
                    "${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_DESCRIPTION}"
                ),
                Pair(
                    "${Taxon.TABLE_NAME}.\"${AbstractTaxon.COLUMN_HERITAGE}\"",
                    "${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_HERITAGE}"
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
}
