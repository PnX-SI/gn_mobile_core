package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.TaxonWithArea.Companion.defaultProjection
import fr.geonature.commons.data.entity.TaxonWithArea.Companion.fromCursor
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.Date

/**
 * Unit tests about [TaxonWithArea].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class TaxonWithAreaTest {

    @MockK
    private lateinit var cursor: Cursor

    @Before
    fun setUp() {
        init(this)

        every { cursor.isClosed } returns false
    }

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
    fun `should create taxon with area from complete cursor`() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            every { cursor.getColumnIndexOrThrow(c.second) } returns index
            every { cursor.getColumnIndex(c.second) } returns index
        }
        every { cursor.getLong(0) } returns 1234
        every { cursor.getString(1) } returns "taxon_01"
        every { cursor.getString(2) } returns "Animalia"
        every { cursor.getString(3) } returns "Ascidies"
        every { cursor.getString(4) } returns "taxon_01_common"
        every { cursor.getString(5) } returns "desc"
        every { cursor.getString(6) } returns "ES - 1234"
        every { cursor.getLong(7) } returns 1234
        every { cursor.getLong(8) } returns 10
        every { cursor.getString(9) } returns "red"
        every { cursor.getInt(10) } returns 3
        every { cursor.getLong(11) } returns 1477642500000

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
    fun `should create taxon with area from partial cursor`() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in TaxonArea.defaultProjection() -> {
                    every { cursor.getColumnIndexOrThrow(c.second) }.throws(IllegalArgumentException())
                    every { cursor.getColumnIndex(c.second) } returns -1
                }
                else -> {
                    every { cursor.getColumnIndexOrThrow(c.second) } returns index
                    every { cursor.getColumnIndex(c.second) } returns index
                }
            }
        }
        every { cursor.getLong(0) } returns 1234
        every { cursor.getString(1) } returns "taxon_01"
        every { cursor.getString(2) } returns "Animalia"
        every { cursor.getString(3) } returns "Ascidies"
        every { cursor.getString(4) } returns null
        every { cursor.getString(5) } returns "desc"
        every { cursor.getString(6) } returns null

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
    fun `should create taxon with area from incomplete cursor`() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in TaxonArea.defaultProjection() -> {
                    every { cursor.getColumnIndexOrThrow(c.second) } returns index
                    every { cursor.getColumnIndex(c.second) } returns -1
                }
                else -> {
                    every { cursor.getColumnIndexOrThrow(c.second) } returns index
                    every { cursor.getColumnIndex(c.second) } returns index
                }
            }
        }
        every { cursor.getLong(0) } returns 1234
        every { cursor.getString(1) } returns "taxon_01"
        every { cursor.getString(2) } returns "Animalia"
        every { cursor.getString(3) } returns "Ascidies"
        every { cursor.getString(4) } returns null
        every { cursor.getString(5) } returns "desc"
        every { cursor.getString(6) } returns null
        every { cursor.getLong(7) } returns 0
        every { cursor.getLong(8) } returns 0

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
    fun `should return a null taxon from closed cursor`() {
        // given a mocked Cursor
        every { cursor.isClosed } returns true

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = fromCursor(cursor)

        // then
        assertNull(taxonWithArea)
    }

    @Test
    fun `should return a null taxon from invalid cursor`() {
        // given a mocked Cursor
        defaultProjection().forEach { c ->
            every { cursor.getColumnIndexOrThrow(c.second) }.throws(IllegalArgumentException())
            every { cursor.getColumnIndex(c.second) } returns -1
        }

        // when getting a TaxonWithArea instance from Cursor
        val taxonWithArea = fromCursor(cursor)

        // then
        assertNull(taxonWithArea)
    }

    @Test
    fun `should create taxon with area from Parcelable`() {
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
    fun `should build default projection`() {
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
    fun `should build filter by name or description or rank from simple query string with area colors`() {
        val taxonFilterByNameAndAreaColors = (TaxonWithArea
            .Filter()
            .byNameOrDescriptionOrRank("as") as TaxonWithArea.Filter)
            .byAreaColors(
                "red",
                "grey'"
            )
            .build()

        assertEquals(
            "(${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME} GLOB ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME_COMMON} GLOB ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_DESCRIPTION} GLOB ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_RANK} LIKE ?) AND (${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_COLOR} IN ('red', 'grey'''))",
            taxonFilterByNameAndAreaColors.first
        )
        assertArrayEquals(
            arrayOf(
                "*[aáàäâãAÁÀÄÂÃ][sS]*",
                "*[aáàäâãAÁÀÄÂÃ][sS]*",
                "*[aáàäâãAÁÀÄÂÃ][sS]*",
                "%as%"
            ),
            taxonFilterByNameAndAreaColors.second
        )
    }

    @Test
    fun `should build filter with only area colors`() {
        val taxonFilterByAreaColors = TaxonWithArea
            .Filter()
            .byAreaColors(
                "red",
                "grey"
            )
            .build()

        assertEquals(
            "(${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_COLOR} IN ('red', 'grey'))",
            taxonFilterByAreaColors.first
        )
        Assert.assertTrue(taxonFilterByAreaColors.second.isEmpty())
    }
}
