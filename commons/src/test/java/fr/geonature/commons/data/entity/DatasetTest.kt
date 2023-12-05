package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.Dataset.Companion.defaultProjection
import fr.geonature.commons.data.entity.Dataset.Companion.fromCursor
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.Date

/**
 * Unit tests about [Dataset].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class DatasetTest {

    @MockK
    private lateinit var cursor: Cursor

    @Before
    fun setUp() {
        init(this)

        every { cursor.isClosed } returns false
    }

    @Test
    fun testEquals() {
        val now = Date.from(Instant.now())

        assertEquals(
            Dataset(
                1234,
                "occtax",
                "Dataset #1",
                "description",
                true,
                now,
                100
            ),
            Dataset(
                1234,
                "occtax",
                "Dataset #1",
                "description",
                true,
                now,
                100
            )
        )
    }

    @Test
    fun testCreateFromCompleteCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            every { cursor.getColumnIndexOrThrow(c.second) } returns index
            every { cursor.getColumnIndex(c.second) } returns index
        }
        every { cursor.getLong(0) } returns 1234
        every { cursor.getString(1) } returns "occtax"
        every { cursor.getString(2) } returns "Dataset #1"
        every { cursor.getString(3) } returns "description"
        every { cursor.getInt(4) } returns 1
        every { cursor.getLong(5) } returns 1477642500000
        every { cursor.getLong(6) } returns 100

        // when getting a dataset instance from Cursor
        val dataset = fromCursor(cursor)

        // then
        assertNotNull(dataset)
        assertEquals(
            Dataset(
                1234,
                "occtax",
                "Dataset #1",
                "description",
                true,
                Date.from(Instant.parse("2016-10-28T08:15:00Z")),
                100
            ),
            dataset
        )
    }

    @Test
    fun testCreateFromClosedCursor() {
        // given a mocked Cursor
        every { cursor.isClosed } returns true

        // when getting InputObserver instance from Cursor
        val dataset = fromCursor(cursor)

        // then
        assertNull(dataset)
    }

    @Test
    fun `should create Dataset from Parcel`() {
        // given a dataset
        val dataset = Dataset(
            1234,
            "occtax",
            "Dataset #1",
            "description",
            true,
            Date.from(Instant.now()),
            100
        )

        // when we obtain a Parcel object to write the dataset instance to it
        val parcel = Parcel.obtain()
        dataset.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            dataset,
            parcelableCreator<Dataset>().createFromParcel(parcel)
        )
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(
            arrayOf(
                Pair(
                    "${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_ID}\"",
                    "${Dataset.TABLE_NAME}_${Dataset.COLUMN_ID}"
                ),
                Pair(
                    "${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_MODULE}\"",
                    "${Dataset.TABLE_NAME}_${Dataset.COLUMN_MODULE}"
                ),
                Pair(
                    "${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_NAME}\"",
                    "${Dataset.TABLE_NAME}_${Dataset.COLUMN_NAME}"
                ),
                Pair(
                    "${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_DESCRIPTION}\"",
                    "${Dataset.TABLE_NAME}_${Dataset.COLUMN_DESCRIPTION}"
                ),
                Pair(
                    "${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_ACTIVE}\"",
                    "${Dataset.TABLE_NAME}_${Dataset.COLUMN_ACTIVE}"
                ),
                Pair(
                    "${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_CREATED_AT}\"",
                    "${Dataset.TABLE_NAME}_${Dataset.COLUMN_CREATED_AT}"
                ),
                Pair(
                    "${Dataset.TABLE_NAME}.\"${Dataset.COLUMN_TAXA_LIST_ID}\"",
                    "${Dataset.TABLE_NAME}_${Dataset.COLUMN_TAXA_LIST_ID}"
                )
            ),
            defaultProjection()
        )
    }
}
