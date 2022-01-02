package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.DefaultNomenclatureWithType.Companion.defaultProjection
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
 * Unit tests about [DefaultNomenclatureWithType].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class DefaultNomenclatureWithTypeTest {

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
            DefaultNomenclatureWithType(
                "occtax",
                2
            ),
            DefaultNomenclatureWithType(
                "occtax",
                2
            )
        )

        assertEquals(
            DefaultNomenclatureWithType(
                "occtax",
                2,
                NomenclatureWithType(
                    2,
                    "SN",
                    "1234:002",
                    "label",
                    1234
                )
            ),
            DefaultNomenclatureWithType(
                "occtax",
                2,
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
            DefaultNomenclatureWithType(
                "occtax",
                2,
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
            DefaultNomenclatureWithType(
                "occtax",
                2,
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
    }

    @Test
    fun testCreateFromCompleteCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            every { cursor.getColumnIndexOrThrow(c.second) } returns index
        }
        every { cursor.getLong(0) } returns 1234
        every { cursor.getString(1) } returns "SGR"
        every { cursor.getString(2) } returns "label"
        every { cursor.getLong(3) } returns 2
        every { cursor.getString(4) } returns "SN"
        every { cursor.getString(5) } returns "1234:002"
        every { cursor.getString(6) } returns "label"
        every { cursor.getLong(7) } returns 1234
        every { cursor.getString(8) } returns "occtax"
        every { cursor.getLong(9) } returns 2

        // when getting a nomenclature with taxonomy instance from Cursor
        val defaultNomenclatureWithNomenclature = DefaultNomenclatureWithType.fromCursor(cursor)

        // then
        assertNotNull(defaultNomenclatureWithNomenclature)
        assertEquals(
            DefaultNomenclatureWithType(
                "occtax",
                2,
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
            defaultNomenclatureWithNomenclature
        )
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in NomenclatureWithType.defaultProjection() -> every { cursor.getColumnIndexOrThrow(c.second) }.throws(IllegalArgumentException())
                else -> every { cursor.getColumnIndexOrThrow(c.second) } returns index
            }
        }
        every { cursor.getString(8) } returns "occtax"
        every { cursor.getLong(9) } returns 2

        // when getting a nomenclature with taxonomy instance from Cursor
        val defaultNomenclatureWithNomenclature = DefaultNomenclatureWithType.fromCursor(cursor)

        // then
        assertNotNull(defaultNomenclatureWithNomenclature)
        assertEquals(
            DefaultNomenclatureWithType(
                "occtax",
                2
            ),
            defaultNomenclatureWithNomenclature
        )
    }

    @Test
    fun testParcelable() {
        // given a default nomenclature instance
        val defaultNomenclatureWithNomenclature = DefaultNomenclatureWithType(
            "occtax",
            2,
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

        // when we obtain a Parcel object to write the nomenclature with taxonomy instance to it
        val parcel = Parcel.obtain()
        defaultNomenclatureWithNomenclature.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            defaultNomenclatureWithNomenclature,
            DefaultNomenclatureWithType.CREATOR.createFromParcel(parcel)
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
                    "${DefaultNomenclature.TABLE_NAME}.\"${DefaultNomenclature.COLUMN_MODULE}\"",
                    "${DefaultNomenclature.TABLE_NAME}_${DefaultNomenclature.COLUMN_MODULE}"
                ),
                Pair(
                    "${DefaultNomenclature.TABLE_NAME}.\"${DefaultNomenclature.COLUMN_NOMENCLATURE_ID}\"",
                    "${DefaultNomenclature.TABLE_NAME}_${DefaultNomenclature.COLUMN_NOMENCLATURE_ID}"
                )
            ),
            defaultProjection()
        )
    }
}
