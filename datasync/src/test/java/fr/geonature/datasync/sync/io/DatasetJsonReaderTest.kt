package fr.geonature.datasync.sync.io

import android.app.Application
import fr.geonature.commons.data.entity.Dataset
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
 * Unit tests about [DatasetJsonReader].
 *
 * @author S. Grimault
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
    fun `should read from invalid JSON string`() {
        // when read an invalid JSON
        val dataset = datasetJsonReader.read("")

        // then
        assertNotNull(dataset)
        assertTrue(dataset.isEmpty())
    }

    @Test
    fun `should read empty dataset`() {
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
    fun `should read dataset from valid JSON object string`() {
        // given an input file to read
        val json = getFixture("dataset_geonature.json")

        // when parsing this file
        val dataset = datasetJsonReader.read(json)

        // then
        assertNotNull(dataset)
        assertArrayEquals(
            arrayOf(
                Dataset(
                    18L,
                    "Dataset #1",
                    "Description of Dataset #1",
                    true,
                    datasetJsonReader.toDate("2019-10-30T22:32:16.591174")!!,
                    null,
                    100
                ),
                Dataset(
                    19L,
                    "Dataset #2",
                    "Description of Dataset #2",
                    false,
                    datasetJsonReader.toDate("2019-11-13 10:08:47.762240")!!,
                    datasetJsonReader.toDate("2020-10-28T08:15:00.000000"),
                    null,
                )
            ),
            dataset.toTypedArray()
        )
    }

    @Test
    fun `should read dataset from valid JSON array string`() {
        // given an input file to read
        val json = getFixture("dataset_list_geonature.json")

        // when parsing this file
        val dataset = datasetJsonReader.read(json)

        // then
        assertNotNull(dataset)
        assertArrayEquals(
            arrayOf(
                Dataset(
                    18L,
                    "Dataset #1",
                    "Description of Dataset #1",
                    true,
                    datasetJsonReader.toDate("2019-10-30T22:32:16.591174")!!,
                    null,
                    100
                ),
                Dataset(
                    19L,
                    "Dataset #2",
                    "Description of Dataset #2",
                    false,
                    datasetJsonReader.toDate("2019-11-13 10:08:47.762240")!!,
                    datasetJsonReader.toDate("2020-10-28T08:15:00.000000"),
                    null,
                )
            ),
            dataset.toTypedArray()
        )
    }
}
