package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.NomenclatureWithTaxonomy.Companion.defaultProjection
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
            }
        )
    }

    @Test
    fun testCreateFromCompleteCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
        }

        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("SGR")
        `when`(cursor.getString(2)).thenReturn("label")
        `when`(cursor.getLong(3)).thenReturn(2)
        `when`(cursor.getString(4)).thenReturn("SN")
        `when`(cursor.getString(5)).thenReturn("1234:002")
        `when`(cursor.getString(6)).thenReturn("label")
        `when`(cursor.getLong(7)).thenReturn(1234)
        `when`(cursor.getLong(8)).thenReturn(2)
        `when`(cursor.getString(9)).thenReturn("Animalia")
        `when`(cursor.getString(10)).thenReturn("Ascidies")

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
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in NomenclatureTaxonomy.defaultProjection() -> `when`(cursor.getColumnIndexOrThrow(c.second)).thenThrow(
                    IllegalArgumentException::class.java
                )
                else -> `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
            }
        }

        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("SGR")
        `when`(cursor.getString(2)).thenReturn("label")
        `when`(cursor.getLong(3)).thenReturn(2)
        `when`(cursor.getString(4)).thenReturn("SN")
        `when`(cursor.getString(5)).thenReturn("1234:002")
        `when`(cursor.getString(6)).thenReturn("label")
        `when`(cursor.getLong(7)).thenReturn(1234)

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
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in NomenclatureTaxonomy.defaultProjection() -> `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(
                    index
                )
                else -> `when`(cursor.getColumnIndexOrThrow(c.second)).thenThrow(
                    IllegalArgumentException::class.java
                )
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
