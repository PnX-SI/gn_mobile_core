package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.Taxon.Companion.defaultProjection
import fr.geonature.commons.data.entity.Taxon.Companion.fromCursor
import fr.geonature.commons.data.helper.EntityHelper.column
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [Taxon].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class TaxonTest {

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
            Taxon(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                null,
                "desc"
            ),
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

        assertEquals(
            Taxon(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                "taxon_01_common",
                "desc",
                "ES - 1234"
            ),
            Taxon(
                1234,
                "taxon_01",
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                ),
                "taxon_01_common",
                "desc",
                "ES - 1234"
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
                "taxon_01_common",
                "desc",
                "ES - 1234"
            ),
            taxon
        )
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in arrayOf(
                    column(
                        AbstractTaxon.COLUMN_NAME_COMMON,
                        Taxon.TABLE_NAME
                    ),
                    column(
                        AbstractTaxon.COLUMN_DESCRIPTION,
                        Taxon.TABLE_NAME
                    ),
                    column(
                        AbstractTaxon.COLUMN_RANK,
                        Taxon.TABLE_NAME
                    )
                ) -> {
                    every { cursor.getColumnIndexOrThrow(c.second) } returns -1
                    every { cursor.getColumnIndex(c.second) } returns -1
                }
                else -> {
                    every { cursor.getColumnIndexOrThrow(c.second) } returns index
                }
            }
        }
        every { cursor.getLong(0) } returns 1234
        every { cursor.getString(1) } returns "taxon_01"
        every { cursor.getString(2) } returns "Animalia"
        every { cursor.getString(3) } returns "Ascidies"

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
        every { cursor.isClosed } returns true

        // when getting a Taxon instance from Cursor
        val taxon = fromCursor(cursor)

        // then
        assertNull(taxon)
    }

    @Test
    fun testCreateFromInvalidCursor() {
        // given a mocked Cursor
        defaultProjection().forEach { c ->
            when (c) {
                in arrayOf(
                    column(
                        AbstractTaxon.COLUMN_NAME_COMMON,
                        Taxon.TABLE_NAME
                    ),
                    column(
                        AbstractTaxon.COLUMN_DESCRIPTION,
                        Taxon.TABLE_NAME
                    )
                ) -> {
                    every { cursor.getColumnIndexOrThrow(c.second) } returns -1
                    every { cursor.getColumnIndex(c.second) } returns -1
                }
                else -> {
                    every { cursor.getColumnIndexOrThrow(c.second) }.throws(IllegalArgumentException())
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
            "taxon_01_common",
            "desc",
            "ES - 1234"
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
                )
            ),
            defaultProjection()
        )
    }

    @Test
    fun testFilter() {
        val taxonFilterByNameAndTaxonomy = Taxon
            .Filter()
            .byNameOrDescriptionOrRank("as")
            .byTaxonomy(
                Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            )
            .build()

        assertEquals(
            "(${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME} LIKE ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME_COMMON} LIKE ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_DESCRIPTION} LIKE ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_RANK} LIKE ?) AND ((${Taxon.TABLE_NAME}_${Taxonomy.COLUMN_KINGDOM} = ?) AND (${Taxon.TABLE_NAME}_${Taxonomy.COLUMN_GROUP} = ?))",
            taxonFilterByNameAndTaxonomy.first
        )
        assertArrayEquals(
            arrayOf(
                "%as%",
                "%as%",
                "%as%",
                "%as%",
                "Animalia",
                "Ascidies"
            ),
            taxonFilterByNameAndTaxonomy.second
        )

        val taxonFilterByNameAndKingdom = Taxon
            .Filter()
            .byNameOrDescriptionOrRank("as")
            .byTaxonomy(
                Taxonomy(
                    "Animalia"
                )
            )
            .build()

        assertEquals(
            "(${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME} LIKE ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_NAME_COMMON} LIKE ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_DESCRIPTION} LIKE ? OR ${Taxon.TABLE_NAME}_${AbstractTaxon.COLUMN_RANK} LIKE ?) AND (${Taxon.TABLE_NAME}_${Taxonomy.COLUMN_KINGDOM} = ?)",
            taxonFilterByNameAndKingdom.first
        )
        assertArrayEquals(
            arrayOf(
                "%as%",
                "%as%",
                "%as%",
                "%as%",
                "Animalia"
            ),
            taxonFilterByNameAndKingdom.second
        )

        val taxonFilterByKingdom = Taxon
            .Filter()
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

        val taxonFilterByAnyTaxonomy = Taxon
            .Filter()
            .byTaxonomy(Taxonomy(""))
            .build()

        assertEquals(
            "",
            taxonFilterByAnyTaxonomy.first
        )
        assertTrue(taxonFilterByAnyTaxonomy.second.isEmpty())
    }
}
