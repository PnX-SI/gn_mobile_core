package fr.geonature.commons.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

/**
 * Unit test for `DateHelper`.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DateHelperTest {

    @Test
    fun testToDate() {
        assertNull(toDate(null))
        assertNull(toDate(""))
        assertNull(toDate("no_such_valid_date"))

        val isoDateTime = toDate("2016-10-28T08:15:00Z")
        assertNotNull(isoDateTime)

        val sdfDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        sdfDateTime.timeZone = TimeZone.getTimeZone("UTC")
        assertEquals(
            "2016-10-28T08:15:00",
            sdfDateTime.format(isoDateTime!!)
        )

        val isoDate = toDate("2016-10-28")
        assertNotNull(isoDate)

        val sdfDate = SimpleDateFormat("yyyy-MM-dd")
        sdfDate.timeZone = TimeZone.getTimeZone("UTC")
        assertEquals(
            "2016-10-28",
            sdfDate.format(isoDate!!)
        )
    }

    @Test
    fun testToIsoDateString() {
        assertEquals(
            "2016-10-28T08:15:00Z",
            toDate("2016-10-28T08:15:00Z")?.toIsoDateString()
        )
    }

    @Test
    fun testAddCalendarField() {
        assertEquals(
            "2016-10-28T09:10:00Z",
            toDate("2016-10-28T08:15:00Z")?.add(
                Calendar.HOUR,
                1
            )
                ?.add(
                    Calendar.MINUTE,
                    -5
                )
                ?.toIsoDateString()
        )
    }
}
