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
 * @author S. Grimault
 */
class DateHelperTest {

    @Test
    fun `should parse date string to Date`() {
        assertNull(toDate(null))
        assertNull(toDate(""))
        assertNull(toDate("no_such_valid_date"))

        val isoDateTime = toDate("2016-10-28T08:15:00Z")
        assertNotNull(isoDateTime)
        assertEquals("2016-10-28T08:15:00",
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").let {
                it.timeZone = TimeZone.getTimeZone("UTC")
                it.format(isoDateTime!!)
            })

        val isoDate = toDate("2016-10-28")
        assertNotNull(isoDate)
        assertEquals("2016-10-28",
            SimpleDateFormat("yyyy-MM-dd").let {
                it.timeZone = TimeZone.getTimeZone("UTC")
                it.format(isoDate!!)
            })

        val hourOnlyDate = toDate("08:15")
        assertNotNull(hourOnlyDate)
        assertEquals("08:15",
            SimpleDateFormat("HH:mm").let {
                it.timeZone = TimeZone.getTimeZone("UTC")
                it.format(hourOnlyDate!!)
            })
    }

    @Test
    fun `should format date using custom pattern`() {
        assertEquals(
            "2016-10-28",
            toDate("2016-10-28T08:15:00Z")?.format("yyyy-MM-dd")
        )
        assertEquals(
            "08:15",
            toDate("2016-10-28T08:15:00Z")?.format("HH:mm")
        )
    }

    @Test
    fun `should format date to ISO-8601`() {
        assertEquals(
            "2016-10-28T08:15:00Z",
            toDate("2016-10-28T08:15:00Z")?.toIsoDateString()
        )
    }

    @Test
    fun `should get calendar field value`() {
        assertEquals(
            2016,
            toDate("2016-10-28T08:15:00Z")?.get(Calendar.YEAR)
        )
        assertEquals(
            9,
            toDate("2016-10-28T08:15:00Z")?.get(Calendar.MONTH)
        )
        assertEquals(
            28,
            toDate("2016-10-28T08:15:00Z")?.get(Calendar.DAY_OF_MONTH)
        )
        assertEquals(
            8,
            toDate("2016-10-28T08:15:00Z")?.get(Calendar.HOUR_OF_DAY)
        )
        assertEquals(
            15,
            toDate("2016-10-28T08:15:00Z")?.get(Calendar.MINUTE)
        )
    }

    @Test
    fun `should add value to date field`() {
        assertEquals(
            "2016-10-28T09:10:00Z",
            toDate("2016-10-28T08:15:00Z")
                ?.add(
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

    @Test
    fun `should set value to date field`() {
        assertEquals(
            "2016-10-28T08:15:00Z",
            toDate("2016-10-28T09:00:00Z")
                ?.set(
                    Calendar.HOUR,
                    8
                )
                ?.set(
                    Calendar.MINUTE,
                    15
                )
                ?.toIsoDateString()
        )
    }
}
