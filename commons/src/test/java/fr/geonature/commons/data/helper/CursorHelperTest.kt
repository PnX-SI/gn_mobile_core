package fr.geonature.commons.data.helper

import android.database.Cursor
import androidx.core.database.getDoubleOrNull
import androidx.core.database.getFloatOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getShortOrNull
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

/**
 * Unit tests about `CursorHelper`.
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class CursorHelperTest {

    @MockK
    private lateinit var cursor: Cursor

    @Before
    fun setUp() {
        init(this)

        every { cursor.isClosed } returns false
    }

    @Test
    fun `should get byte array from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getBlob(0) } returns byteArrayOf(
            2,
            8
        )

        assertArrayEquals(
            byteArrayOf(
                2,
                8
            ),
            cursor.get<ByteArray>("column_name")
        )
    }

    @Test
    fun `should get null value as ByteArray from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getBlob(0) } returns null

        assertNull(cursor.get<ByteArray>("column_name"))
    }

    @Test
    fun `should get default value as ByteArray from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getColumnIndex("column_name") } returns 0
        every { cursor.getBlob(0) } returns null

        assertArrayEquals(
            byteArrayOf(
                2,
                8
            ),
            cursor.get<ByteArray>(
                "column_name",
                byteArrayOf(
                    2,
                    8
                )
            )
        )
    }

    @Test
    fun `should get string value from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getString(0) } returns "some string"

        assertEquals(
            "some string",
            cursor.get<String>("column_name")
        )
    }

    @Test
    fun `should get null value as String from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getString(0) } returns null

        assertNull(cursor.get<String>("column_name"))
    }

    @Test
    fun `should get default value as String from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getColumnIndex("column_name") } returns 0
        every { cursor.getString(0) } returns null

        assertEquals(
            "default value",
            cursor.get<String>(
                "column_name",
                "default value"
            )
        )
    }

    @Test
    fun `should get short value from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getShortOrNull(0) } returns 2.toShort()
        every { cursor.isNull(0) } returns false

        assertEquals(
            2.toShort(),
            cursor.get<Short>("column_name")
        )
    }

    @Test
    fun `should get null value as Short from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getShortOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertNull(cursor.get<Short>("column_name"))
    }

    @Test
    fun `should get default value as Short from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getColumnIndex("column_name") } returns 0
        every { cursor.getShortOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertEquals(
            8.toShort(),
            cursor.get<Short>(
                "column_name",
                8.toShort()
            )
        )
    }

    @Test
    fun `should get int value from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getIntOrNull(0) } returns 2
        every { cursor.isNull(0) } returns false

        assertEquals(
            2,
            cursor.get<Int>("column_name")
        )
    }

    @Test
    fun `should get null value as Int from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getIntOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertNull(cursor.get<Int>("column_name"))
    }

    @Test
    fun `should get default value as Int from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getColumnIndex("column_name") } returns 0
        every { cursor.getIntOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertEquals(
            8,
            cursor.get<Int>(
                "column_name",
                8
            )
        )
    }

    @Test
    fun `should get long value from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getLongOrNull(0) } returns 2L
        every { cursor.isNull(0) } returns false

        assertEquals(
            2L,
            cursor.get<Long>("column_name")
        )
    }

    @Test
    fun `should get null value as Long from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getLongOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertNull(cursor.get<Long>("column_name"))
    }

    @Test
    fun `should get default value as Long from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getColumnIndex("column_name") } returns 0
        every { cursor.getLongOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertEquals(
            8L,
            cursor.get<Long>(
                "column_name",
                8L
            )
        )
    }

    @Test
    fun `should get float value from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getFloatOrNull(0) } returns 2F
        every { cursor.isNull(0) } returns false

        assertEquals(
            2F,
            cursor.get<Float>("column_name")
        )
    }

    @Test
    fun `should get null value as Float from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getFloatOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertNull(cursor.get<Float>("column_name"))
    }

    @Test
    fun `should get default value as Float from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getColumnIndex("column_name") } returns 0
        every { cursor.getFloatOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertEquals(
            8F,
            cursor.get<Float>(
                "column_name",
                8F
            )
        )
    }

    @Test
    fun `should get double value from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getDoubleOrNull(0) } returns 2.5
        every { cursor.isNull(0) } returns false

        assertEquals(
            2.5,
            cursor.get<Double>("column_name")
        )
    }

    @Test
    fun `should get null value as Double from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getDoubleOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertNull(cursor.get<Double>("column_name"))
    }

    @Test
    fun `should get default value as Double from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getColumnIndex("column_name") } returns 0
        every { cursor.getDoubleOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertEquals(
            3.14,
            cursor.get<Double>(
                "column_name",
                3.14
            )
        )
    }

    @Test
    fun `should get boolean value as true from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getIntOrNull(0) } returns 1
        every { cursor.isNull(0) } returns false

        assertEquals(
            true,
            cursor.get<Boolean>("column_name")
        )
    }

    @Test
    fun `should get boolean value as false from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getIntOrNull(0) } returns 0
        every { cursor.isNull(0) } returns false

        assertEquals(
            false,
            cursor.get<Boolean>("column_name")
        )
    }

    @Test
    fun `should get null value as Boolean from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getIntOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertNull(cursor.get<Boolean>("column_name"))
    }

    @Test
    fun `should get default value as Boolean from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getColumnIndex("column_name") } returns 0
        every { cursor.getIntOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertEquals(
            true,
            cursor.get<Boolean>(
                "column_name",
                true
            )
        )
    }

    @Test
    fun `should get date value from Cursor`() {
        val expectedDate = Date()
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getLongOrNull(0) } returns expectedDate.time
        every { cursor.isNull(0) } returns false

        assertEquals(
            expectedDate,
            cursor.get<Date>("column_name")
        )
    }

    @Test
    fun `should get null value as Date from Cursor`() {
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getLongOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertNull(cursor.get<Date>("column_name"))
    }

    @Test
    fun `should get default value as Date from Cursor`() {
        val expectedDate = Date()
        every { cursor.getColumnIndexOrThrow("column_name") } returns 0
        every { cursor.getColumnIndex("column_name") } returns 0
        every { cursor.getLongOrNull(0) } returns null
        every { cursor.isNull(0) } returns true

        assertEquals(
            expectedDate,
            cursor.get<Date>(
                "column_name",
                expectedDate
            )
        )
    }
}