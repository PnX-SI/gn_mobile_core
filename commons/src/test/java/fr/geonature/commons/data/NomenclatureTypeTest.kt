package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.NomenclatureType.Companion.fromCursor
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
        assertEquals(NomenclatureType(1234,
                                      "SGR",
                                      "label"),
                     NomenclatureType(1234,
                                      "SGR",
                                      "label"))
    }

    @Test
    fun testCreateFromCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(NomenclatureType.COLUMN_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(NomenclatureType.COLUMN_MNEMONIC)).thenReturn(1)
        `when`(cursor.getColumnIndexOrThrow(NomenclatureType.COLUMN_DEFAULT_LABEL)).thenReturn(2)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("SGR")
        `when`(cursor.getString(2)).thenReturn("label")

        // when getting a nomenclature type instance from Cursor
        val nomenclatureType = fromCursor(cursor)

        // then
        assertNotNull(nomenclatureType)
        assertEquals(NomenclatureType(1234,
                                      "SGR",
                                      "label"),
                     nomenclatureType)
    }

    @Test
    fun testParcelable() {
        // given a nomenclature type instance
        val nomenclatureType = NomenclatureType(1234,
                                                "SGR",
                                                "label")

        // when we obtain a Parcel object to write the nomenclature type instance to it
        val parcel = Parcel.obtain()
        nomenclatureType.writeToParcel(parcel,
                                       0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(nomenclatureType,
                     NomenclatureType.CREATOR.createFromParcel(parcel))
    }
}