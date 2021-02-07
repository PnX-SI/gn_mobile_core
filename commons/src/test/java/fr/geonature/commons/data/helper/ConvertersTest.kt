package fr.geonature.commons.data.helper

import fr.geonature.commons.data.helper.Converters.dateToTimestamp
import fr.geonature.commons.data.helper.Converters.fromTimestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.util.Date

/**
 * Unit tests about [Converters].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class ConvertersTest {

    @Test
    fun testFromTimestamp() {
        assertNull(fromTimestamp(null))
        assertEquals(
            Date.from(Instant.parse("2016-10-28T08:15:00Z")),
            fromTimestamp(1477642500000)
        )
    }

    @Test
    fun testDateToTimestamp() {
        assertNull(dateToTimestamp(null))
        assertEquals(
            1477642500000,
            dateToTimestamp(Date.from(Instant.parse("2016-10-28T08:15:00Z")))
        )
    }
}
