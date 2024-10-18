package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.NomenclatureWithTaxonomy.Companion.defaultProjection
import fr.geonature.commons.data.entity.NomenclatureWithTaxonomy.Companion.fromCursor
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [NomenclatureWithTaxonomy].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class NomenclatureWithTaxonomyTest {

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
            NomenclatureWithTaxonomy(
                NomenclatureWithType(
                    2,
                    "SN",
                    "1234:002",
                    "label",
                    1234
                )
            ),
            NomenclatureWithTaxonomy(
                NomenclatureWithType(
                    2,
                    "SN",
                    "1234:002",
                    "label",
                    1234
                )
            )
        )

        assertEquals(
            NomenclatureWithTaxonomy(
                NomenclatureWithType(
                    2,
                    "SN",
                    "1234:002",
                    "label",
                    1234,
                    NomenclatureType(
                        1234,
                        "SGR",
                        "label"
                    )
                )
            ),
            NomenclatureWithTaxonomy(
                NomenclatureWithType(
                    2,
                    "SN",
                    "1234:002",
                    "label",
                    1234,
                    NomenclatureType(
                        1234,
                        "SGR",
                        "label"
                    )
                )
            )
        )

        assertEquals(NomenclatureWithTaxonomy(
            NomenclatureWithType(
                2,
                "SN",
                "1234:002",
                "label",
                1234,
                NomenclatureType(
                    1234,
                    "SGR",
                    "label"
                )
            )
        ).also {
            it.taxonony = Taxonomy(
                "Animalia",
                "Ascidies"
            )
        },
            NomenclatureWithTaxonomy(
                NomenclatureWithType(
                    2,
                    "SN",
                    "1234:002",
                    "label",
                    1234,
                    NomenclatureType(
                        1234,
                        "SGR",
                        "label"
                    )
                )
            ).also {
                it.taxonony = Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            })
    }

    @Test
    fun testCreateFromCompleteCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            every { cursor.getColumnIndexOrThrow(c.second) } returns index
        }
        every { cursor.getLong(0) } returns 1234
        every { cursor.isNull(0) } returns false
        every { cursor.getString(1) } returns "SGR"
        every { cursor.getString(2) } returns "label"
        every { cursor.getLong(3) } returns 2
        every { cursor.isNull(3) } returns false
        every { cursor.getString(4) } returns "SN"
        every { cursor.getString(5) } returns "1234:002"
        every { cursor.getString(6) } returns "label"
        every { cursor.getLong(7) } returns 1234
        every { cursor.isNull(7) } returns false
        every { cursor.getLong(8) } returns 2
        every { cursor.isNull(8) } returns false
        every { cursor.getString(9) } returns "Animalia"
        every { cursor.getString(10) } returns "Ascidies"

        // when getting a nomenclature with taxonomy instance from Cursor
        val nomenclatureWithTaxonomy = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureWithTaxonomy)
        assertEquals(
            NomenclatureWithTaxonomy(
                NomenclatureWithType(
                    2,
                    "SN",
                    "1234:002",
                    "label",
                    1234,
                    NomenclatureType(
                        1234,
                        "SGR",
                        "label"
                    )
                )
            ).also {
                it.taxonony = Taxonomy(
                    "Animalia",
                    "Ascidies"
                )
            },
            nomenclatureWithTaxonomy
        )
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in NomenclatureTaxonomy.defaultProjection() -> every { cursor.getColumnIndexOrThrow(c.second) }.throws(IllegalArgumentException())
                else -> every { cursor.getColumnIndexOrThrow(c.second) } returns index
            }
        }
        every { cursor.getLong(0) } returns 1234
        every { cursor.isNull(0) } returns false
        every { cursor.getString(1) } returns "SGR"
        every { cursor.getString(2) } returns "label"
        every { cursor.getLong(3) } returns 2
        every { cursor.isNull(3) } returns false
        every { cursor.getString(4) } returns "SN"
        every { cursor.getString(5) } returns "1234:002"
        every { cursor.getString(6) } returns "label"
        every { cursor.getLong(7) } returns 1234
        every { cursor.isNull(7) } returns false

        // when getting a nomenclature with taxonomy instance from Cursor
        val nomenclatureWithTaxonomy = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureWithTaxonomy)
        assertEquals(
            NomenclatureWithTaxonomy(
                NomenclatureWithType(
                    2,
                    "SN",
                    "1234:002",
                    "label",
                    1234,
                    NomenclatureType(
                        1234,
                        "SGR",
                        "label"
                    )
                )
            ),
            nomenclatureWithTaxonomy
        )
    }

    @Test
    fun testCreateFromInvalidCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in NomenclatureTaxonomy.defaultProjection() -> every { cursor.getColumnIndexOrThrow(c.second) } returns index
                else -> every { cursor.getColumnIndexOrThrow(c.second) }.throws(IllegalArgumentException())
            }
        }

        // when getting a nomenclature with taxonomy instance from Cursor
        val nomenclatureWithTaxonomy = fromCursor(cursor)

        // then
        assertNull(nomenclatureWithTaxonomy)
    }

    @Test
    fun testParcelable() {
        // given a nomenclature with taxonomy instance
        val nomenclatureWithTaxonomy = NomenclatureWithTaxonomy(
            NomenclatureWithType(
                2,
                "SN",
                "1234:002",
                "label",
                1234,
                NomenclatureType(
                    1234,
                    "SGR",
                    "label"
                )
            )
        ).also {
            it.taxonony = Taxonomy(
                "Animalia",
                "Ascidies"
            )
        }

        // when we obtain a Parcel object to write the nomenclature with taxonomy instance to it
        val parcel = Parcel.obtain()
        nomenclatureWithTaxonomy.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            nomenclatureWithTaxonomy,
            NomenclatureWithTaxonomy.CREATOR.createFromParcel(parcel)
        )
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(
            arrayOf(
                Pair(
                    "${NomenclatureType.TABLE_NAME}.\"${NomenclatureType.COLUMN_ID}\"",
                    "${NomenclatureType.TABLE_NAME}_${NomenclatureType.COLUMN_ID}"
                ),
                Pair(
                    "${NomenclatureType.TABLE_NAME}.\"${NomenclatureType.COLUMN_MNEMONIC}\"",
                    "${NomenclatureType.TABLE_NAME}_${NomenclatureType.COLUMN_MNEMONIC}"
                ),
                Pair(
                    "${NomenclatureType.TABLE_NAME}.\"${NomenclatureType.COLUMN_DEFAULT_LABEL}\"",
                    "${NomenclatureType.TABLE_NAME}_${NomenclatureType.COLUMN_DEFAULT_LABEL}"
                ),
                Pair(
                    "${Nomenclature.TABLE_NAME}.\"${Nomenclature.COLUMN_ID}\"",
                    "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_ID}"
                ),
                Pair(
                    "${Nomenclature.TABLE_NAME}.\"${Nomenclature.COLUMN_CODE}\"",
                    "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_CODE}"
                ),
                Pair(
                    "${Nomenclature.TABLE_NAME}.\"${Nomenclature.COLUMN_HIERARCHY}\"",
                    "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_HIERARCHY}"
                ),
                Pair(
                    "${Nomenclature.TABLE_NAME}.\"${Nomenclature.COLUMN_DEFAULT_LABEL}\"",
                    "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_DEFAULT_LABEL}"
                ),
                Pair(
                    "${Nomenclature.TABLE_NAME}.\"${Nomenclature.COLUMN_TYPE_ID}\"",
                    "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_TYPE_ID}"
                ),
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
