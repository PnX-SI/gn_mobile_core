package fr.geonature.commons.data.helper

import fr.geonature.commons.data.helper.ProviderHelper.buildUri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [ProviderHelper].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class ProviderHelperTest {

    @Test
    fun testBuildUri() {
        val authority = "fr.geonature.sync.provider"

        assertEquals(
            "content://$authority/taxa",
            buildUri(
                authority,
                "taxa"
            ).toString()
        )

        assertEquals(
            "content://$authority/taxa/123",
            buildUri(
                authority,
                "taxa",
                123.toString()
            ).toString()
        )

        assertEquals(
            "content://$authority/taxa/area/123",
            buildUri(
                authority,
                "taxa",
                "area/123"
            ).toString()
        )

        assertEquals(
            "content://$authority/taxa/area/123",
            buildUri(
                authority,
                "taxa",
                "area",
                123.toString()
            ).toString()
        )

        assertEquals(
            "content://$authority/taxa/area/123",
            buildUri(
                authority,
                "taxa",
                "area",
                "",
                123.toString()
            ).toString()
        )

        assertEquals(
            "content://$authority/taxa/area/123",
            buildUri(
                authority,
                "taxa",
                "area",
                "  ",
                123.toString()
            ).toString()
        )
    }
}
