package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.Nomenclature.Companion.defaultProjection
import fr.geonature.commons.data.entity.Nomenclature.Companion.fromCursor
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
 * Unit tests about [Nomenclature].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class NomenclatureTest {

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
            Nomenclature(
                2,
                "SN",
                "1234:002",
                "label",
                1234
            ),
            Nomenclature(
                2,
                "SN",
                "1234:002",
                "label",
                1234
            )
        )
    }

    @Test
    fun testCreateFromCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            every { cursor.getColumnIndexOrThrow(c.second) } returns index
        }
        every { cursor.getLong(0) } returns 2
        every { cursor.isNull(0) } returns false
        every { cursor.getString(1) } returns "SN"
        every { cursor.getString(2) } returns "1234:002"
        every { cursor.getString(3) } returns "label"
        every { cursor.getLong(4) } returns 1234
        every { cursor.isNull(4) } returns false

        // when getting a nomenclature instance from Cursor
        val nomenclature = fromCursor(cursor)

        // then
        assertNotNull(nomenclature)
        assertEquals(
            Nomenclature(
                2,
                "SN",
                "1234:002",
                "label",
                1234
            ),
            nomenclature
        )
    }

    @Test
    fun testParcelable() {
        // given a nomenclature instance
        val nomenclature = Nomenclature(
            2,
            "SN",
            "1234:002",
            "label",
            1234
        )

        // when we obtain a Parcel object to write the nomenclature instance to it
        val parcel = Parcel.obtain()
        nomenclature.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            nomenclature,
            Nomenclature.CREATOR.createFromParcel(parcel)
        )
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(
            arrayOf(
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
