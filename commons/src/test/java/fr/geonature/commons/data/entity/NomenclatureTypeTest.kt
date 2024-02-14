package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.NomenclatureType.Companion.defaultProjection
import fr.geonature.commons.data.entity.NomenclatureType.Companion.fromCursor
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
 * Unit tests about [NomenclatureType].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class NomenclatureTypeTest {

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
            NomenclatureType(
                1234,
                "SGR",
                "label"
            ),
            NomenclatureType(
                1234,
                "SGR",
                "label"
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
        every { cursor.getString(1) } returns "SGR"
        every { cursor.getString(2) } returns "label"

        // when getting a nomenclature type instance from Cursor
        val nomenclatureType = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureType)
        assertEquals(
            NomenclatureType(
                1234,
                "SGR",
                "label"
            ),
            nomenclatureType
        )
    }

    @Test
    fun testParcelable() {
        // given a nomenclature type instance
        val nomenclatureType = NomenclatureType(
            1234,
            "SGR",
            "label"
        )

        // when we obtain a Parcel object to write the nomenclature type instance to it
        val parcel = Parcel.obtain()
        nomenclatureType.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            nomenclatureType,
            NomenclatureType.CREATOR.createFromParcel(parcel)
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
                )
            ),
            defaultProjection()
        )
    }
}
