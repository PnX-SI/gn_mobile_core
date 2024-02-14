package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.NomenclatureWithType.Companion.defaultProjection
import fr.geonature.commons.data.entity.NomenclatureWithType.Companion.fromCursor
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
 * Unit tests about [NomenclatureWithType].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class NomenclatureWithTypeTest {

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
            NomenclatureWithType(
                2,
                "SN",
                "1234:002",
                "label",
                1234
            ),
            NomenclatureWithType(
                2,
                "SN",
                "1234:002",
                "label",
                1234
            )
        )

        assertEquals(
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
            ),
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

        assertEquals(
            NomenclatureWithType(
                Nomenclature(
                    2,
                    "SN",
                    "1234:002",
                    "label",
                    1234
                )
            ),
            NomenclatureWithType(
                Nomenclature(
                    2,
                    "SN",
                    "1234:002",
                    "label",
                    1234
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

        // when getting a nomenclature with type instance from Cursor
        val nomenclatureWithType = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureWithType)
        assertEquals(
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
            ),
            nomenclatureWithType
        )
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        Nomenclature
            .defaultProjection()
            .forEachIndexed { index, c ->
                every { cursor.getColumnIndexOrThrow(c.second) } returns index
            }
        NomenclatureType
            .defaultProjection()
            .forEach { c ->
                every { cursor.getColumnIndexOrThrow(c.second) }.throws(IllegalArgumentException())
            }
        every { cursor.getLong(0) } returns 2
        every { cursor.isNull(0) } returns false
        every { cursor.getString(1) } returns "SN"
        every { cursor.getString(2) } returns "1234:002"
        every { cursor.getString(3) } returns "label"
        every { cursor.getLong(4) } returns 1234
        every { cursor.isNull(4) } returns false

        // when getting a nomenclature with type instance from Cursor
        val nomenclatureWithType = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureWithType)
        assertEquals(
            NomenclatureWithType(
                2,
                "SN",
                "1234:002",
                "label",
                1234
            ),
            nomenclatureWithType
        )
    }

    @Test
    fun testCreateFromInvalidCursor() {
        // given a mocked Cursor
        defaultProjection().forEach { c ->
            every { cursor.getColumnIndexOrThrow(c.second) }.throws(IllegalArgumentException())
        }

        // when getting a nomenclature with type instance from Cursor
        val nomenclatureWithType = fromCursor(cursor)

        // then
        assertNull(nomenclatureWithType)
    }

    @Test
    fun testParcelable() {
        // given a nomenclature with type instance
        val nomenclatureWithType = NomenclatureWithType(
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

        // when we obtain a Parcel object to write the nomenclature with type instance to it
        val parcel = Parcel.obtain()
        nomenclatureWithType.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            nomenclatureWithType,
            NomenclatureWithType.CREATOR.createFromParcel(parcel)
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
                )
            ),
            defaultProjection()
        )
    }
}
