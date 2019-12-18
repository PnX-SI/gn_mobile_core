package fr.geonature.commons.data.helper

import fr.geonature.commons.data.helper.Provider.buildUri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [Provider].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class ProviderTest {

    @Test
    fun testBuildUri() {
        assertEquals("content://${Provider.AUTHORITY}/taxa",
                     buildUri("taxa").toString())

        assertEquals("content://${Provider.AUTHORITY}/taxa/123",
                     buildUri("taxa",
                              123.toString()).toString())

        assertEquals("content://${Provider.AUTHORITY}/taxa/area/123",
                     buildUri("taxa",
                              "area/${123}").toString())

        assertEquals("content://${Provider.AUTHORITY}/taxa/area/123",
                     buildUri("taxa",
                              "area",
                              123.toString()).toString())

        assertEquals("content://${Provider.AUTHORITY}/taxa/area/123",
                     buildUri("taxa",
                              "area",
                              "",
                              123.toString()).toString())

        assertEquals("content://${Provider.AUTHORITY}/taxa/area/123",
                     buildUri("taxa",
                              "area",
                              "  ",
                              123.toString()).toString())
    }
}