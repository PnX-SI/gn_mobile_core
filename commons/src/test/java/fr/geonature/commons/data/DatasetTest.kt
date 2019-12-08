package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.Dataset.Companion.defaultProjection
import fr.geonature.commons.data.Dataset.Companion.fromCursor
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.Date

/**
 * Unit tests about [Dataset].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class DatasetTest {

    @Test
    fun testEquals() {
        val now = Date.from(Instant.now())

        assertEquals(Dataset(1234,
                             "Dataset #1",
                             "description",
                             true,
                             now),
                     Dataset(1234,
                             "Dataset #1",
                             "description",
                             true,
                             now))
    }

    @Test
    fun testCreateFromCompleteCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
            `when`(cursor.getColumnIndex(c.second)).thenReturn(index)
        }

        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("Dataset #1")
        `when`(cursor.getString(2)).thenReturn("description")
        `when`(cursor.getInt(3)).thenReturn(1)
        `when`(cursor.getLong(4)).thenReturn(1477642500000)

        // when getting a dataset instance from Cursor
        val dataset = fromCursor(cursor)

        // then
        assertNotNull(dataset)
        assertEquals(Dataset(1234,
                             "Dataset #1",
                             "description",
                             true,
                             Date.from(Instant.parse("2016-10-28T08:15:00Z"))),
                     dataset)
    }

    @Test
    fun testCreateFromClosedCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.isClosed).thenReturn(true)

        // when getting InputObserver instance from Cursor
        val dataset = fromCursor(cursor)

        // then
        assertNull(dataset)
    }

    @Test
    fun testParcelable() {
        // given a dataset
        val dataset = Dataset(1234,
                              "Dataset #1",
                              "description",
                              true,
                              Date.from(Instant.now()))

        // when we obtain a Parcel object to write the dataset instance to it
        val parcel = Parcel.obtain()
        dataset.writeToParcel(parcel,
                              0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(dataset,
                     Dataset.CREATOR.createFromParcel(parcel))
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(arrayOf(Pair("${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_ID}\"",
                                       "${Dataset.TABLE_NAME}_${Dataset.COLUMN_ID}"),
                                  Pair("${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_NAME}\"",
                                       "${Dataset.TABLE_NAME}_${Dataset.COLUMN_NAME}"),
                                  Pair("${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_DESCRIPTION}\"",
                                       "${Dataset.TABLE_NAME}_${Dataset.COLUMN_DESCRIPTION}"),
                                  Pair("${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_ACTIVE}\"",
                                       "${Dataset.TABLE_NAME}_${Dataset.COLUMN_ACTIVE}"),
                                  Pair("${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_CREATED_AT}\"",
                                       "${Dataset.TABLE_NAME}_${Dataset.COLUMN_CREATED_AT}")),
                          defaultProjection())
    }
}