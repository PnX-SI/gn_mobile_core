package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.NomenclatureType.Companion.defaultProjection
import fr.geonature.commons.data.NomenclatureType.Companion.fromCursor
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [NomenclatureType].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class NomenclatureTypeTest {

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
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
        }

        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("SGR")
        `when`(cursor.getString(2)).thenReturn("label")

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
