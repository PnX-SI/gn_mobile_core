package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.InputObserver.Companion.defaultProjection
import fr.geonature.commons.data.entity.InputObserver.Companion.fromCursor
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
 * Unit tests about [InputObserver].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class InputObserverTest {

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
            InputObserver(
                1234,
                "lastname",
                "firstname"
            ),
            InputObserver(
                1234,
                "lastname",
                "firstname"
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
        every { cursor.getString(1) } returns "lastname"
        every { cursor.getString(2) } returns "firstname"

        // when getting InputObserver instance from Cursor
        val inputObserver = fromCursor(cursor)

        // then
        assertNotNull(inputObserver)
        assertEquals(
            InputObserver(
                1234,
                "lastname",
                "firstname"
            ),
            inputObserver
        )
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            every { cursor.getColumnIndexOrThrow(c.second) } returns index
        }
        every { cursor.getLong(0) } returns 1234
        every { cursor.getString(1) } returns null
        every { cursor.getString(2) } returns null

        // when getting InputObserver instance from Cursor
        val inputObserver = fromCursor(cursor)

        // then
        assertNotNull(inputObserver)
        assertEquals(
            InputObserver(
                1234,
                null,
                null
            ),
            inputObserver
        )
    }

    @Test
    fun testCreateFromInvalidCursor() {
        // given a mocked Cursor
        defaultProjection().forEach { c ->
            every { cursor.getColumnIndexOrThrow(c.second) }.throws(IllegalArgumentException())
        }
        every { cursor.getLong(0) } returns 0
        every { cursor.getString(1) } returns null
        every { cursor.getString(2) } returns null

        // when getting InputObserver instance from Cursor
        val inputObserver = fromCursor(cursor)

        // then
        assertNull(inputObserver)
    }

    @Test
    fun testCreateFromClosedCursor() {
        // given a mocked Cursor
        every { cursor.isClosed } returns true

        // when getting InputObserver instance from Cursor
        val inputObserver = fromCursor(cursor)

        // then
        assertNull(inputObserver)
    }

    @Test
    fun testParcelable() {
        // given InputObserver
        val inputObserver = InputObserver(
            1234,
            "lastname",
            "firstname"
        )

        // when we obtain a Parcel object to write the InputObserver instance to it
        val parcel = Parcel.obtain()
        inputObserver.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            inputObserver,
            InputObserver.CREATOR.createFromParcel(parcel)
        )
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(
            arrayOf(
                Pair(
                    "${InputObserver.TABLE_NAME}.\"${InputObserver.COLUMN_ID}\"",
                    "${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_ID}"
                ),
                Pair(
                    "${InputObserver.TABLE_NAME}.\"${InputObserver.COLUMN_LASTNAME}\"",
                    "${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_LASTNAME}"
                ),
                Pair(
                    "${InputObserver.TABLE_NAME}.\"${InputObserver.COLUMN_FIRSTNAME}\"",
                    "${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_FIRSTNAME}"
                )
            ),
            defaultProjection()
        )
    }
}
