package fr.geonature.viewpager.helper

import fr.geonature.viewpager.model.Pager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.StringWriter

/**
 * Unit test for [PagerJsonWriter].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class PagerJsonWriterTest {
    lateinit var pagerJsonWriter: PagerJsonWriter

    @Before
    fun setUp() {
        pagerJsonWriter = PagerJsonWriter()
    }

    @Test
    fun testWriteEmptyPager() {
        // given an empty pager metadata
        val pager = Pager(0)

        // when write this pager as JSON string
        val writer = StringWriter()
        pagerJsonWriter.write(writer, pager)

        // then
        assertNotNull(writer.toString())

        val expectedJsonString = StringBuilder().append('{')
            .append("\"id\":")
            .append(0)
            .append(",\"size\":")
            .append(0)
            .append(",\"position\":")
            .append(0)
            .append(",\"history\":[]")
            .append('}')
            .toString()

        assertEquals(expectedJsonString, writer.toString())
    }

    @Test
    fun testWrite() {
        // given a pager metadata
        val pager = Pager(1234L)
        pager.size = 5
        pager.position = 3
        pager.history.add(1)
        pager.history.add(4)
        pager.history.add(3)
        pager.history.add(2)

        // when write this pager as JSON string
        val writer = StringWriter()
        pagerJsonWriter.write(writer, pager)

        // then
        assertNotNull(writer.toString())

        val expectedJsonString = StringBuilder().append('{')
            .append("\"id\":")
            .append(1234L)
            .append(",\"size\":")
            .append(5)
            .append(",\"position\":")
            .append(3)
            .append(",\"history\":[1,4,3,2]")
            .append('}')
            .toString()

        assertEquals(expectedJsonString, writer.toString())
    }
}