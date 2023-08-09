package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.InputObserver.Companion.defaultProjection
import fr.geonature.commons.data.entity.InputObserver.Companion.fromCursor
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder
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
    fun `should create observer from complete cursor`() {
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
    fun `should create observer from partial cursor`() {
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
    fun `should return a null observer from closed cursor`() {
        // given a mocked Cursor
        every { cursor.isClosed } returns true

        // when getting InputObserver instance from Cursor
        val inputObserver = fromCursor(cursor)

        // then
        assertNull(inputObserver)
    }

    @Test
    fun `should return a null observer from invalid cursor`() {
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
    fun `should create observer from Parcelable`() {
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
    fun `should build default projection`() {
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

    @Test
    fun `should build filter by name from simple query string`() {
        val filterByName = InputObserver
            .Filter()
            .byName("admin'")
            .build()

        assertEquals(
            "(${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_LASTNAME} GLOB ? OR ${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_FIRSTNAME} GLOB ?)",
            filterByName.first
        )
        assertArrayEquals(
            arrayOf(
                "*[aáàäâãAÁÀÄÂÃ][dD][mM][iíìïîĩIÍÌÏÎĨ][nñNÑ]['']*",
                "*[aáàäâãAÁÀÄÂÃ][dD][mM][iíìïîĩIÍÌÏÎĨ][nñNÑ]['']*"
            ),
            filterByName.second
        )
    }

    @Test
    fun `should build order by last name with no query string`() {
        val orderByName = InputObserver
            .OrderBy()
            .byName()
            .build()

        assertEquals(
            "${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_LASTNAME} ${SQLiteSelectQueryBuilder.OrderingTerm.ASC.name}",
            orderByName
        )
    }

    @Test
    fun `should build order by name with query string`() {
        val orderByName = InputObserver
            .OrderBy()
            .byName("admin'")
            .build()

        assertEquals(
            "(CASE WHEN (${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_LASTNAME} = 'admin''' OR ${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_FIRSTNAME} = 'admin''') THEN 1" + " WHEN (${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_LASTNAME} LIKE '%admin''%' OR ${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_FIRSTNAME} LIKE '%admin''%') THEN 2" + " WHEN (${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_LASTNAME} GLOB '*[aáàäâãAÁÀÄÂÃ][dD][mM][iíìïîĩIÍÌÏÎĨ][nñNÑ]['']*' OR ${InputObserver.TABLE_NAME}_${InputObserver.COLUMN_FIRSTNAME} GLOB '*[aáàäâãAÁÀÄÂÃ][dD][mM][iíìïîĩIÍÌÏÎĨ][nñNÑ]['']*') THEN 3" + " ELSE 4 END)",
            orderByName
        )
    }
}
