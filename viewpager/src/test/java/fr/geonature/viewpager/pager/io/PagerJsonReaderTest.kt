package fr.geonature.viewpager.pager.io

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.StringReader

/**
 * Unit test for [PagerJsonReader].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class PagerJsonReaderTest {
    private lateinit var pagerJsonReader: PagerJsonReader

    @Before
    fun setUp() {
        pagerJsonReader = PagerJsonReader()
    }

    @Test
    fun testReadEmptyPager() {
        // given a JSON string
        val jsonString = StringBuilder().append('{')
            .append("\"id\":")
            .append(0)
            .append(",\"size\":")
            .append(0)
            .append(",\"position\":")
            .append(0)
            .append(",\"history\":[]")
            .append('}')
            .toString()

        // when read this JSON string
        val reader = StringReader(jsonString)
        val pager = pagerJsonReader.read(reader)

        // then
        assertNotNull(pager)
        assertEquals(
            0,
            pager.id
        )
        assertEquals(
            0,
            pager.size
        )
        assertEquals(
            0,
            pager.position
        )
        assertTrue(pager.history.isEmpty())
    }

    @Test
    fun testRead() {
        // given a JSON string
        val jsonString = StringBuilder().append('{')
            .append("\"id\":")
            .append(1234L)
            .append(",\"size\":")
            .append(5)
            .append(",\"position\":")
            .append(3)
            .append(",\"history\":[1,4,3,2]")
            .append('}')
            .toString()

        // when read this JSON string
        val reader = StringReader(jsonString)
        val pager = pagerJsonReader.read(reader)

        // then
        assertNotNull(pager)
        assertEquals(
            1234L,
            pager.id
        )
        assertEquals(
            5,
            pager.size
        )
        assertEquals(
            3,
            pager.position
        )
        assertEquals(
            4,
            pager.history.size
        )
        assertEquals(
            Integer.valueOf(2),
            pager.history.pollLast()
        )
        assertEquals(
            Integer.valueOf(3),
            pager.history.pollLast()
        )
        assertEquals(
            Integer.valueOf(4),
            pager.history.pollLast()
        )
        assertEquals(
            Integer.valueOf(1),
            pager.history.pollLast()
        )
    }
}
