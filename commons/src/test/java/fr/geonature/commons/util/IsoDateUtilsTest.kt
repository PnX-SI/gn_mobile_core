package fr.geonature.commons.util

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unit test for [IsoDateUtils].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class IsoDateUtilsTest {

    @Test
    fun toDate() {
        assertNull(IsoDateUtils.toDate(null))
        assertNull(IsoDateUtils.toDate(""))
        assertNull(IsoDateUtils.toDate("no_such_valid_date"))

        val isoDate = IsoDateUtils.toDate("2016-10-28T08:15:00Z")
        assertNotNull(isoDate)

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        assertEquals("2016-10-28T08:15:00", sdf.format(isoDate))
    }
}