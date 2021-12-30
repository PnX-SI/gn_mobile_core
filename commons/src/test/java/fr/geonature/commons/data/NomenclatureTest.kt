package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.model.Nomenclature
import fr.geonature.commons.data.model.Nomenclature.Companion.defaultProjection
import fr.geonature.commons.data.model.Nomenclature.Companion.fromCursor
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [Nomenclature].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class NomenclatureTest {

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
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
        }

        `when`(cursor.getLong(0)).thenReturn(2)
        `when`(cursor.getString(1)).thenReturn("SN")
        `when`(cursor.getString(2)).thenReturn("1234:002")
        `when`(cursor.getString(3)).thenReturn("label")
        `when`(cursor.getLong(4)).thenReturn(1234)

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
