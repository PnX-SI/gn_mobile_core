package fr.geonature.sync.sync.io

import android.app.Application
import fr.geonature.commons.data.Dataset
import fr.geonature.sync.FixtureHelper.getFixture
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests about [DatasetJsonReader].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class DatasetJsonReaderTest {

    private lateinit var datasetJsonReader: DatasetJsonReader

    @Before
    fun setUp() {
        datasetJsonReader = DatasetJsonReader()
    }

    @Test
    fun testReadFromInvalidJsonString() {
        // when read an invalid JSON
        val dataset = datasetJsonReader.read("")

        // then
        assertNotNull(dataset)
        assertTrue(dataset.isEmpty())
    }

    @Test
    fun testReadEmptyDataset() {
        // when read an empty JSON
        var dataset = datasetJsonReader.read("{}")

        // then
        assertNotNull(dataset)
        assertTrue(dataset.isEmpty())

        // when read a JSON with empty data
        dataset = datasetJsonReader.read("{\"data\":[]}")

        // then
        assertNotNull(dataset)
        assertTrue(dataset.isEmpty())
    }

    @Test
    fun testRead() {
        // given an input file to read
        val json = getFixture("metadataset_geonature.json")

        // when parsing this file
        val dataset = datasetJsonReader.read(json)

        // then
        assertNotNull(dataset)
        assertArrayEquals(
            arrayOf(
                Dataset(
                    18L,
                    "occtax",
                    "Dataset #1",
                    "Description of Dataset #1",
                    true,
                    datasetJsonReader.toDate("2019-10-30 22:32:16.591174")
                ),
                Dataset(
                    19L,
                    "occtax",
                    "Dataset #2",
                    "Description of Dataset #2",
                    false,
                    datasetJsonReader.toDate("2019-11-13 10:08:47.762240")
                ),
                Dataset(
                    19L,
                    "occhab",
                    "Dataset #2",
                    "Description of Dataset #2",
                    false,
                    datasetJsonReader.toDate("2019-11-13 10:08:47.762240")
                )
            ),
            dataset.toTypedArray()
        )
    }
}
