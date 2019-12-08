package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.DefaultNomenclatureWithType.Companion.defaultProjection
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [DefaultNomenclatureWithType].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class DefaultNomenclatureWithTypeTest {

    @Test
    fun testEquals() {
        assertEquals(DefaultNomenclatureWithType("occtax",
                                                 2),
                     DefaultNomenclatureWithType("occtax",
                                                 2))

        assertEquals(DefaultNomenclatureWithType("occtax",
                                                 2,
                                                 NomenclatureWithType(2,
                                                                      "SN",
                                                                      "1234:002",
                                                                      "label",
                                                                      1234)),
                     DefaultNomenclatureWithType("occtax",
                                                 2,
                                                 NomenclatureWithType(2,
                                                                      "SN",
                                                                      "1234:002",
                                                                      "label",
                                                                      1234)))

        assertEquals(DefaultNomenclatureWithType("occtax",
                                                 2,
                                                 NomenclatureWithType(2,
                                                                      "SN",
                                                                      "1234:002",
                                                                      "label",
                                                                      1234,
                                                                      NomenclatureType(1234,
                                                                                       "SGR",
                                                                                       "label"))),
                     DefaultNomenclatureWithType("occtax",
                                                 2,
                                                 NomenclatureWithType(2,
                                                                      "SN",
                                                                      "1234:002",
                                                                      "label",
                                                                      1234,
                                                                      NomenclatureType(1234,
                                                                                       "SGR",
                                                                                       "label"))))
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
        `when`(cursor.getString(8)).thenReturn("occtax")
        `when`(cursor.getLong(9)).thenReturn(2)

        // when getting a nomenclature with taxonomy instance from Cursor
        val defaultNomenclatureWithNomenclature = DefaultNomenclatureWithType.fromCursor(cursor)

        // then
        assertNotNull(defaultNomenclatureWithNomenclature)
        assertEquals(DefaultNomenclatureWithType("occtax",
                                                 2,
                                                 NomenclatureWithType(2,
                                                                      "SN",
                                                                      "1234:002",
                                                                      "label",
                                                                      1234,
                                                                      NomenclatureType(1234,
                                                                                       "SGR",
                                                                                       "label"))),
                     defaultNomenclatureWithNomenclature)
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in NomenclatureWithType.defaultProjection() -> `when`(cursor.getColumnIndexOrThrow(c.second)).thenThrow(IllegalArgumentException::class.java)
                else -> `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
            }
        }

        `when`(cursor.getString(8)).thenReturn("occtax")
        `when`(cursor.getLong(9)).thenReturn(2)

        // when getting a nomenclature with taxonomy instance from Cursor
        val defaultNomenclatureWithNomenclature = DefaultNomenclatureWithType.fromCursor(cursor)

        // then
        assertNotNull(defaultNomenclatureWithNomenclature)
        assertEquals(DefaultNomenclatureWithType("occtax",
                                                 2),
                     defaultNomenclatureWithNomenclature)
    }

    @Test
    fun testParcelable() {
        // given a default nomenclature instance
        val defaultNomenclatureWithNomenclature = DefaultNomenclatureWithType("occtax",
                                                                              2,
                                                                              NomenclatureWithType(2,
                                                                                                   "SN",
                                                                                                   "1234:002",
                                                                                                   "label",
                                                                                                   1234,
                                                                                                   NomenclatureType(1234,
                                                                                                                    "SGR",
                                                                                                                    "label")))

        // when we obtain a Parcel object to write the nomenclature with taxonomy instance to it
        val parcel = Parcel.obtain()
        defaultNomenclatureWithNomenclature.writeToParcel(parcel,
                                                          0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(defaultNomenclatureWithNomenclature,
                     DefaultNomenclatureWithType.CREATOR.createFromParcel(parcel))
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(arrayOf(Pair("${NomenclatureType.TABLE_NAME}.${NomenclatureType.COLUMN_ID}",
                                       "${NomenclatureType.TABLE_NAME}_${NomenclatureType.COLUMN_ID}"),
                                  Pair("${NomenclatureType.TABLE_NAME}.${NomenclatureType.COLUMN_MNEMONIC}",
                                       "${NomenclatureType.TABLE_NAME}_${NomenclatureType.COLUMN_MNEMONIC}"),
                                  Pair("${NomenclatureType.TABLE_NAME}.${NomenclatureType.COLUMN_DEFAULT_LABEL}",
                                       "${NomenclatureType.TABLE_NAME}_${NomenclatureType.COLUMN_DEFAULT_LABEL}"),
                                  Pair("${Nomenclature.TABLE_NAME}.${Nomenclature.COLUMN_ID}",
                                       "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_ID}"),
                                  Pair("${Nomenclature.TABLE_NAME}.${Nomenclature.COLUMN_CODE}",
                                       "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_CODE}"),
                                  Pair("${Nomenclature.TABLE_NAME}.${Nomenclature.COLUMN_HIERARCHY}",
                                       "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_HIERARCHY}"),
                                  Pair("${Nomenclature.TABLE_NAME}.${Nomenclature.COLUMN_DEFAULT_LABEL}",
                                       "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_DEFAULT_LABEL}"),
                                  Pair("${Nomenclature.TABLE_NAME}.${Nomenclature.COLUMN_TYPE_ID}",
                                       "${Nomenclature.TABLE_NAME}_${Nomenclature.COLUMN_TYPE_ID}"),
                                  Pair("${DefaultNomenclature.TABLE_NAME}.${DefaultNomenclature.COLUMN_MODULE}",
                                       "${DefaultNomenclature.TABLE_NAME}_${DefaultNomenclature.COLUMN_MODULE}"),
                                  Pair("${DefaultNomenclature.TABLE_NAME}.${DefaultNomenclature.COLUMN_NOMENCLATURE_ID}",
                                       "${DefaultNomenclature.TABLE_NAME}_${DefaultNomenclature.COLUMN_NOMENCLATURE_ID}")),
                          defaultProjection())
    }

}