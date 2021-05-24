package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.AppSync.Companion.defaultProjection
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * Unit tests about [AppSync].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class AppSyncTest {

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
        val cursor = mock(Cursor::class.java)

        defaultProjection().forEachIndexed { index, c ->
            `when`(cursor.getColumnIndexOrThrow(c.second)).thenReturn(index)
            `when`(cursor.getColumnIndex(c.second)).thenReturn(index)
        }

        `when`(cursor.getString(0)).thenReturn("fr.geonature.sync")
        `when`(cursor.getLong(1)).thenReturn(1477638900000)
        `when`(cursor.getLong(2)).thenReturn(1477642500000)
        `when`(cursor.getInt(3)).thenReturn(3)

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
