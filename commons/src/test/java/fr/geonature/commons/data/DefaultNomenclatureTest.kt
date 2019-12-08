package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.DefaultNomenclature.Companion.defaultProjection
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [DefaultNomenclature].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class DefaultNomenclatureTest {

    @Test
    fun testEquals() {
        assertEquals(DefaultNomenclature("occtax",
                                         1234),
                     DefaultNomenclature("occtax",
                                         1234))
    }

    @Test
    fun testCreateFromCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
        }

        `when`(cursor.getString(0)).thenReturn("occtax")
        `when`(cursor.getLong(1)).thenReturn(1234)

        // when getting a DefaultNomenclature instance from Cursor
        val defaultNomenclature = DefaultNomenclature.fromCursor(cursor)

        // then
        assertNotNull(defaultNomenclature)
        assertEquals(DefaultNomenclature("occtax",
                                         1234),
                     defaultNomenclature)
    }

    @Test
    fun testParcelable() {
        // given a default nomenclature instance
        val defaultNomenclature = DefaultNomenclature("occtax",
                                                      1234)

        // when we obtain a Parcel object to write this default nomenclature instance to it
        val parcel = Parcel.obtain()
        defaultNomenclature.writeToParcel(parcel,
                                          0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(defaultNomenclature,
                     DefaultNomenclature.CREATOR.createFromParcel(parcel))
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(arrayOf(Pair("${DefaultNomenclature.TABLE_NAME}.${DefaultNomenclature.COLUMN_MODULE}",
                                       "${DefaultNomenclature.TABLE_NAME}_${DefaultNomenclature.COLUMN_MODULE}"),
                                  Pair("${DefaultNomenclature.TABLE_NAME}.${DefaultNomenclature.COLUMN_NOMENCLATURE_ID}",
                                       "${DefaultNomenclature.TABLE_NAME}_${DefaultNomenclature.COLUMN_NOMENCLATURE_ID}")),
                          defaultProjection())
    }
}