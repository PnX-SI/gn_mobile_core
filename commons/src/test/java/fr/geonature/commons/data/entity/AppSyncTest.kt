package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.AppSync.Companion.defaultProjection
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Unit tests about [AppSync].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class AppSyncTest {

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
            AppSync(
                "fr.geonature.sync",
                Date.from(
                    now
                        .toInstant()
                        .minus(
                            1,
                            ChronoUnit.HOURS
                        )
                ),
                now,
                3
            ),
            AppSync(
                "fr.geonature.sync",
                Date.from(
                    now
                        .toInstant()
                        .minus(
                            1,
                            ChronoUnit.HOURS
                        )
                ),
                now,
                3
            )
        )
    }

    @Test
    fun testCreateFromCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            every { cursor.getColumnIndexOrThrow(c.second) } returns index
            every { cursor.getColumnIndex(c.second) } returns index
        }
        every { cursor.getString(0) } returns "fr.geonature.sync"
        every { cursor.getLong(1) } returns 1477638900000
        every { cursor.getLong(2) } returns 1477642500000
        every { cursor.getInt(3) } returns 3

        // when getting AppSync instance from Cursor
        val appSync = AppSync.fromCursor(cursor)

        // then
        assertNotNull(appSync)
        assertEquals(
            AppSync(
                "fr.geonature.sync",
                Date.from(Instant.parse("2016-10-28T07:15:00Z")),
                Date.from(Instant.parse("2016-10-28T08:15:00Z")),
                3
            ),
            appSync
        )
    }

    @Test
    fun testParcelable() {
        // given AppSync
        val appSync = AppSync(
            "fr.geonature.sync",
            Date.from(
                Instant
                    .now()
                    .minus(
                        1,
                        ChronoUnit.HOURS
                    )
            ),
            Date.from(Instant.now()),
            3
        )

        // when we obtain a Parcel object to write the AppSync instance to it
        val parcel = Parcel.obtain()
        appSync.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            appSync,
            AppSync.CREATOR.createFromParcel(parcel)
        )
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(
            arrayOf(
                Pair(
                    "${AppSync.TABLE_NAME}.\"${AppSync.COLUMN_ID}\"",
                    "${AppSync.TABLE_NAME}_${AppSync.COLUMN_ID}"
                ),
                Pair(
                    "${AppSync.TABLE_NAME}.\"${AppSync.COLUMN_LAST_SYNC}\"",
                    "${AppSync.TABLE_NAME}_${AppSync.COLUMN_LAST_SYNC}"
                ),
                Pair(
                    "${AppSync.TABLE_NAME}.\"${AppSync.COLUMN_LAST_SYNC_ESSENTIAL}\"",
                    "${AppSync.TABLE_NAME}_${AppSync.COLUMN_LAST_SYNC_ESSENTIAL}"
                ),
                Pair(
                    "${AppSync.TABLE_NAME}.\"${AppSync.COLUMN_INPUTS_TO_SYNCHRONIZE}\"",
                    "${AppSync.TABLE_NAME}_${AppSync.COLUMN_INPUTS_TO_SYNCHRONIZE}"
                )
            ),
            defaultProjection()
        )
    }
}
