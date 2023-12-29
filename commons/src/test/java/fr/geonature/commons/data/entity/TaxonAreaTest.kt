package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.TaxonArea.Companion.defaultProjection
import fr.geonature.commons.data.entity.TaxonArea.Companion.fromCursor
import fr.geonature.commons.data.helper.EntityHelper.column
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
import java.time.Instant
import java.util.Date

/**
 * Unit tests about [TaxonArea].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class TaxonAreaTest {

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
            TaxonArea(
                1234,
                10,
                "red",
                3,
                now
            ),
            TaxonArea(
                1234,
                10,
                "red",
                3,
                now
            )
        )

        assertEquals(
            TaxonArea(
                1234,
                10,
                null,
                3,
                now
            ),
            TaxonArea(
                1234,
                10,
                null,
                3,
                now
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
        every { cursor.getLong(1) } returns 10
        every { cursor.getString(2) } returns "red"
        every { cursor.getInt(3) } returns 3
        every { cursor.getLong(4) } returns 1477642500000
        every { cursor.isNull(4) } returns false

        // when getting a TaxonArea instance from Cursor
        val taxonArea = fromCursor(cursor)

        // then
        assertNotNull(taxonArea)
        assertEquals(
            TaxonArea(
                1234,
                10,
                "red",
                3,
                Date.from(Instant.parse("2016-10-28T08:15:00Z"))
            ),
            taxonArea
        )
    }

    @Test
    fun testCreateFromPartialCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            when (c) {
                in arrayOf(
                    column(
                        TaxonArea.COLUMN_COLOR,
                        TaxonArea.TABLE_NAME
                    ),
                    column(
                        TaxonArea.COLUMN_NUMBER_OF_OBSERVERS,
                        TaxonArea.TABLE_NAME
                    ),
                    column(
                        TaxonArea.COLUMN_LAST_UPDATED_AT,
                        TaxonArea.TABLE_NAME
                    )
                ) -> {
                    every { cursor.getColumnIndexOrThrow(c.second) } returns -1
                    every { cursor.getColumnIndex(c.second) } returns -1
                }
                else -> {
                    every { cursor.getColumnIndexOrThrow(c.second) } returns index
                }
            }
        }
        every { cursor.getLong(0) } returns 1234
        every { cursor.getLong(1) } returns 10
        every { cursor.getString(2) } returns null
        every { cursor.getInt(3) } returns 0
        every { cursor.getLong(4) } returns 0

        // when getting a TaxonArea instance from Cursor
        val taxonArea = fromCursor(cursor)

        // then
        assertNotNull(taxonArea)
        assertEquals(
            TaxonArea(
                1234,
                10,
                "#00000000",
                0,
                null
            ),
            taxonArea
        )
    }

    @Test
    fun testCreateFromClosedCursor() {
        // given a mocked Cursor
        every { cursor.isClosed } returns true

        // when getting a TaxonArea instance from Cursor
        val taxonArea = fromCursor(cursor)

        // then
        assertNull(taxonArea)
    }

    @Test
    fun testCreateFromInvalidCursor() {
        // given a mocked Cursor
        defaultProjection().forEach { c ->
            when (c) {
                in arrayOf(
                    column(
                        TaxonArea.COLUMN_COLOR,
                        TaxonArea.TABLE_NAME
                    ),
                    column(
                        TaxonArea.COLUMN_NUMBER_OF_OBSERVERS,
                        TaxonArea.TABLE_NAME
                    ),
                    column(
                        TaxonArea.COLUMN_LAST_UPDATED_AT,
                        TaxonArea.TABLE_NAME
                    )
                ) -> {
                    every { cursor.getColumnIndexOrThrow(c.second) } returns -1
                    every { cursor.getColumnIndex(c.second) } returns -1
                }
                else -> {
                    every { cursor.getColumnIndexOrThrow(c.second) }.throws(IllegalArgumentException())
                }
            }
        }
        every { cursor.getLong(0) } returns 0
        every { cursor.getLong(1) } returns 0
        every { cursor.getString(2) } returns null
        every { cursor.getInt(3) } returns 0
        every { cursor.getLong(4) } returns 0

        // when getting a TaxonArea instance from Cursor
        val taxonArea = fromCursor(cursor)

        // then
        assertNull(taxonArea)
    }

    @Test
    fun testParcelable() {
        // given a TaxonArea
        val taxonArea = TaxonArea(
            1234,
            10,
            "red",
            3,
            Date.from(Instant.now())
        )

        // when we obtain a Parcel object to write the TaxonWithArea instance to it
        val parcel = Parcel.obtain()
        taxonArea.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            taxonArea,
            TaxonArea.CREATOR.createFromParcel(parcel)
        )
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(
            arrayOf(
                Pair(
                    "${TaxonArea.TABLE_NAME}.\"${TaxonArea.COLUMN_TAXON_ID}\"",
                    "${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_TAXON_ID}"
                ),
                Pair(
                    "${TaxonArea.TABLE_NAME}.\"${TaxonArea.COLUMN_AREA_ID}\"",
                    "${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_AREA_ID}"
                ),
                Pair(
                    "${TaxonArea.TABLE_NAME}.\"${TaxonArea.COLUMN_COLOR}\"",
                    "${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_COLOR}"
                ),
                Pair(
                    "${TaxonArea.TABLE_NAME}.\"${TaxonArea.COLUMN_NUMBER_OF_OBSERVERS}\"",
                    "${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_NUMBER_OF_OBSERVERS}"
                ),
                Pair(
                    "${TaxonArea.TABLE_NAME}.\"${TaxonArea.COLUMN_LAST_UPDATED_AT}\"",
                    "${TaxonArea.TABLE_NAME}_${TaxonArea.COLUMN_LAST_UPDATED_AT}"
                )
            ),
            defaultProjection()
        )
    }
}
