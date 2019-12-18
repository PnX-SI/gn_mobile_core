package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.NomenclatureWithType.Companion.defaultProjection
import fr.geonature.commons.data.NomenclatureWithType.Companion.fromCursor
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
 * Unit tests about [NomenclatureWithType].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class NomenclatureWithTypeTest {

    @Test
    fun testEquals() {
        assertEquals(NomenclatureWithType(2,
                                          "SN",
                                          "1234:002",
                                          "label",
                                          1234),
                     NomenclatureWithType(2,
                                          "SN",
                                          "1234:002",
                                          "label",
                                          1234))

        assertEquals(NomenclatureWithType(2,
                                          "SN",
                                          "1234:002",
                                          "label",
                                          1234,
                                          NomenclatureType(1234,
                                                           "SGR",
                                                           "label")),
                     NomenclatureWithType(2,
                                          "SN",
                                          "1234:002",
                                          "label",
                                          1234,
                                          NomenclatureType(1234,
                                                           "SGR",
                                                           "label")))

        assertEquals(NomenclatureWithType(Nomenclature(2,
                                                       "SN",
                                                       "1234:002",
                                                       "label",
                                                       1234)),
                     NomenclatureWithType(Nomenclature(2,
                                                       "SN",
                                                       "1234:002",
                                                       "label",
                                                       1234)))
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

        // when getting a nomenclature with type instance from Cursor
        val nomenclatureWithType = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureWithType)
        assertEquals(NomenclatureWithType(2,
                                          "SN",
                                          "1234:002",
                                          "label",
                                          1234,
                                          NomenclatureType(1234,
                                                           "SGR",
                                                           "label")),
                     nomenclatureWithType)
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        Nomenclature.defaultProjection()
                .forEachIndexed { index, c ->
                    `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
                }
        NomenclatureType.defaultProjection()
                .forEach { c ->
                    `when`(cursor.getColumnIndexOrThrow(c.second)).thenThrow(IllegalArgumentException::class.java)
                }

        `when`(cursor.getLong(0)).thenReturn(2)
        `when`(cursor.getString(1)).thenReturn("SN")
        `when`(cursor.getString(2)).thenReturn("1234:002")
        `when`(cursor.getString(3)).thenReturn("label")
        `when`(cursor.getLong(4)).thenReturn(1234)

        // when getting a nomenclature with type instance from Cursor
        val nomenclatureWithType = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureWithType)
        assertEquals(NomenclatureWithType(2,
                                          "SN",
                                          "1234:002",
                                          "label",
                                          1234),
                     nomenclatureWithType)
    }

    @Test
    fun testCreateFromInvalidCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEach { c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenThrow(IllegalArgumentException::class.java)
        }

        // when getting a nomenclature with type instance from Cursor
        val nomenclatureWithType = fromCursor(cursor)

        // then
        assertNull(nomenclatureWithType)
    }

    @Test
    fun testParcelable() {
        // given a nomenclature with type instance
        val nomenclatureWithType = NomenclatureWithType(2,
                                                        "SN",
                                                        "1234:002",
                                                        "label",
                                                        1234,
                                                        NomenclatureType(1234,
                                                                         "SGR",
                                                                         "label"))

        // when we obtain a Parcel object to write the nomenclature with type instance to it
        val parcel = Parcel.obtain()
        nomenclatureWithType.writeToParcel(parcel,
                                           0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(nomenclatureWithType,
                     NomenclatureWithType.CREATOR.createFromParcel(parcel))
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(arrayOf(Pair("${NomenclatureType.TABLE_NAME}.\"${NomenclatureType.COLUMN_ID}\"",
                                      "${NomenclatureType.TABLE_NAME}_${NomenclatureType.COLUMN_ID}"),
                                  Pair("${NomenclatureType.TABLE_NAME}.\"${NomenclatureType.COLUMN_MNEMONIC}\"",
                                      "${NomenclatureType.TABLE_NAME}_${NomenclatureType.COLUMN_MNEMONIC}"),
                                  Pair("${NomenclatureType.TABLE_NAME}.\"${NomenclatureType.COLUMN_DEFAULT_LABEL}\"",
                                      "${NomenclatureType.TABLE_NAME}_${NomenclatureType.COLUMN_DEFAULT_LABEL}"),
                                  Pair("${Nomenclature.TABLE_NAME}.\"${Nomenclature.COLUMN_ID}\"",
                                      "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_ID}"),
                                  Pair("${Nomenclature.TABLE_NAME}.\"${Nomenclature.COLUMN_CODE}\"",
                                      "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_CODE}"),
                                  Pair("${Nomenclature.TABLE_NAME}.\"${Nomenclature.COLUMN_HIERARCHY}\"",
                                      "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_HIERARCHY}"),
                                  Pair("${Nomenclature.TABLE_NAME}.\"${Nomenclature.COLUMN_DEFAULT_LABEL}\"",
                                      "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_DEFAULT_LABEL}"),
                                  Pair("${Nomenclature.TABLE_NAME}.\"${Nomenclature.COLUMN_TYPE_ID}\"",
                                      "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_TYPE_ID}")),
                          defaultProjection())
    }
}