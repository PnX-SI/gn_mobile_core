package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.InputObserver.Companion.defaultProjection
import fr.geonature.commons.data.InputObserver.Companion.fromCursor
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
 * Unit tests about [InputObserver].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputObserverTest {

    @Test
    fun testEquals() {
        assertEquals(InputObserver(1234,
                                   "lastname",
                                   "firstname"),
                     InputObserver(1234,
                                   "lastname",
                                   "firstname"))
    }

    @Test
    fun testCreateFromCompleteCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
        }

        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("lastname")
        `when`(cursor.getString(2)).thenReturn("firstname")

        // when getting InputObserver instance from Cursor
        val inputObserver = fromCursor(cursor)

        // then
        assertNotNull(inputObserver)
        assertEquals(InputObserver(1234,
                                   "lastname",
                                   "firstname"),
                     inputObserver)
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
        }

        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn(null)
        `when`(cursor.getString(2)).thenReturn(null)

        // when getting InputObserver instance from Cursor
        val inputObserver = fromCursor(cursor)

        // then
        assertNotNull(inputObserver)
        assertEquals(InputObserver(1234,
                                   null,
                                   null),
                     inputObserver)
    }

    @Test
    fun testCreateFromInvalidCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEach { c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenThrow(IllegalArgumentException::class.java)
        }

        `when`(cursor.getLong(0)).thenReturn(0)
        `when`(cursor.getString(1)).thenReturn(null)
        `when`(cursor.getString(2)).thenReturn(null)

        // when getting InputObserver instance from Cursor
        val inputObserver = fromCursor(cursor)

        // then
        assertNull(inputObserver)
    }

    @Test
    fun testCreateFromClosedCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.isClosed).thenReturn(true)

        // when getting InputObserver instance from Cursor
        val inputObserver = fromCursor(cursor)

        // then
        assertNull(inputObserver)
    }

    @Test
    fun testParcelable() {
        // given InputObserver
        val inputObserver = InputObserver(1234,
                                          "lastname",
                                          "firstname")

        // when we obtain a Parcel object to write the InputObserver instance to it
        val parcel = Parcel.obtain()
        inputObserver.writeToParcel(parcel,
                                    0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(inputObserver,
                     InputObserver.CREATOR.createFromParcel(parcel))
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(arrayOf(Pair("${InputObserver.TABLE_NAME}.\"${InputObserver.COLUMN_ID}\"",
                                       "${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_ID}"),
                                  Pair("${InputObserver.TABLE_NAME}.\"${InputObserver.COLUMN_LASTNAME}\"",
                                       "${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_LASTNAME}"),
                                  Pair("${InputObserver.TABLE_NAME}.\"${InputObserver.COLUMN_FIRSTNAME}\"",
                                       "${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_FIRSTNAME}")),
                          defaultProjection())
    }
}