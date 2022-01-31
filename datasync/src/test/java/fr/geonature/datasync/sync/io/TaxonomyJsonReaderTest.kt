package fr.geonature.datasync.sync.io

import android.app.Application
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.datasync.FixtureHelper.getFixture
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests about [TaxonomyJsonReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class TaxonomyJsonReaderTest {

    private lateinit var taxonomyJsonReader: TaxonomyJsonReader

    @Before
    fun setUp() {
        taxonomyJsonReader = TaxonomyJsonReader()
    }

    @Test
    fun testReadFromInvalidJsonString() {
        // when read an invalid JSON
        val taxonomy = taxonomyJsonReader.read("")

        // then
        assertNotNull(taxonomy)
        assertTrue(taxonomy.isEmpty())
    }

    @Test
    fun testReadEmptyTaxonomy() {
        // when read an empty JSON
        val taxonomy = taxonomyJsonReader.read("{}")

        // then
        assertNotNull(taxonomy)
        assertArrayEquals(
            arrayOf(
                Taxonomy(
                    Taxonomy.ANY,
                    Taxonomy.ANY
                )
            ),
            taxonomy.toTypedArray()
        )
    }

    @Test
    fun testRead() {
        // given an input file to read
        val json = getFixture("taxonomy_geonature.json")

        // when parsing this file
        val taxonomy = taxonomyJsonReader.read(json)

        // then
        assertNotNull(taxonomy)
        assertArrayEquals(
            arrayOf(
                Taxonomy.ANY,
                "Animalia",
                "Bacteria",
                "Chromista",
                "Fungi",
                "Plantae",
                "Protozoa"
            ),
            taxonomy
                .map { it.kingdom }
                .distinct()
                .toTypedArray()
        )
        assertArrayEquals(arrayOf(
            Taxonomy.ANY,
            "Lichens"
        ),
            taxonomy
                .asSequence()
                .filter { it.kingdom == "Fungi" }
                .map { it.group }
                .toList()
                .toTypedArray()
        )
    }
}
