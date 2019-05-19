package fr.geonature.commons.util

import fr.geonature.commons.util.IsoDateUtils.toDate
import fr.geonature.commons.util.IsoDateUtils.toIsoDateString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.TimeZone

/**
 * Unit test for [IsoDateUtils].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class IsoDateUtilsTest {

    @Test
    fun testToDate() {
        assertNull(toDate(null))
        assertNull(toDate(""))
        assertNull(toDate("no_such_valid_date"))

        val isoDate = toDate("2016-10-28T08:15:00Z")
        assertNotNull(isoDate)

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        assertEquals("2016-10-28T08:15:00",
                     sdf.format(isoDate))
    }

    @Test
    fun testToIsoDateString() {
        assertNull(toIsoDateString(null))
        assertEquals("2016-10-28T08:15:00Z",
                     toIsoDateString(toDate("2016-10-28T08:15:00Z")))
    }
}