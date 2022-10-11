package fr.geonature.commons.features.input.data

import android.app.Application
import android.util.JsonReader
import android.util.JsonWriter
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.domain.DummyInput
import fr.geonature.commons.features.input.error.InputException
import fr.geonature.commons.features.input.io.InputJsonReader
import fr.geonature.commons.features.input.io.InputJsonWriter
import fr.geonature.commons.settings.DummyAppSettings
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
import io.mockk.MockKAnnotations.init
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Unit tests about [IInputLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class InputLocalDataSourceTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var application: Application
    private lateinit var onInputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<DummyInput>
    private lateinit var onInputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<DummyInput, DummyAppSettings>
    private lateinit var inputLocalDataSource: IInputLocalDataSource<DummyInput, DummyAppSettings>

    @Before
    fun setUp() {
        init(this)

        application = ApplicationProvider.getApplicationContext()

        onInputJsonReaderListener = object : InputJsonReader.OnInputJsonReaderListener<DummyInput> {
            override fun createInput(): DummyInput {
                return DummyInput()
            }

            override fun readAdditionalInputData(
                reader: JsonReader,
                keyName: String,
                input: DummyInput
            ) {
                reader.skipValue()
            }
        }
        onInputJsonWriterListener =
            object : InputJsonWriter.OnInputJsonWriterListener<DummyInput, DummyAppSettings> {
                override fun writeAdditionalInputData(
                    writer: JsonWriter,
                    input: DummyInput,
                    settings: DummyAppSettings?
                ) {
                }
            }

        inputLocalDataSource = InputLocalDataSourceImpl(
            application,
            onInputJsonReaderListener,
            object : InputJsonWriter.OnInputJsonWriterListener<DummyInput, DummyAppSettings> {
                override fun writeAdditionalInputData(
                    writer: JsonWriter,
                    input: DummyInput,
                    settings: DummyAppSettings?
                ) {
                    writer
                        .name("status")
                        .value(input.status.name.lowercase())
                }
            },
            coroutineTestRule.testDispatcher
        )

        PreferenceManager
            .getDefaultSharedPreferences(application)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `should return an empty list when reading undefined inputs`() =
        runTest {
            // when reading non existing inputs
            val noSuchInputs = inputLocalDataSource.readInputs()

            // then
            assertTrue(noSuchInputs.isEmpty())
        }

    @Test
    fun `should save and read inputs`() =
        runTest {
            // given some inputs to save and read
            val input1 = DummyInput().apply { id = 1234 }
            val input2 = DummyInput().apply { id = 1235 }
            val input3 = DummyInput().apply { id = 1237 }

            inputLocalDataSource.saveInput(input1)
            inputLocalDataSource.saveInput(input2)
            inputLocalDataSource.saveInput(input3)

            // and some inputs ready to synchronize
            val input4 = DummyInput().apply {
                id = 1236
                status = AbstractInput.Status.TO_SYNC
            }
            File(
                FileUtils
                    .getInputsFolder(application)
                    .also { it.mkdirs() },
                "input_${input4.id}.json"
            )
                .bufferedWriter()
                .use { out ->
                    out.write(InputJsonWriter(onInputJsonWriterListener).write(input4))
                    out.flush()
                    out.close()
                }

            // when reading these inputs from local data source
            val inputs = inputLocalDataSource.readInputs()

            // then
            assertArrayEquals(arrayOf(
                input1.id to input1.status,
                input2.id to input2.status,
                input4.id to input4.status,
                input3.id to input3.status
            ),
                inputs
                    .map { it.id to it.status }
                    .toTypedArray())
        }

    @Test
    fun `should throw NotFoundException if trying to read undefined input`() =
        runTest {
            val exception = try {
                inputLocalDataSource.readInput(1234)
            } catch (e: Exception) {
                e
            }

            assertTrue(exception is InputException.NotFoundException)
            assertEquals(
                (exception as InputException.NotFoundException).message,
                InputException.NotFoundException(1234).message
            )
        }

    @Test
    fun `should save and read input`() =
        runTest {
            // given an Input to save and read
            val input = DummyInput().apply { id = 1234 }

            // when saving this Input
            val savedInput = inputLocalDataSource.saveInput(input)

            // when reading this Input from local data source
            val inputFromLocalDataSource = inputLocalDataSource.readInput(input.id)

            // then
            assertEquals(
                savedInput.id,
                inputFromLocalDataSource.id
            )
            assertEquals(
                savedInput.module,
                inputFromLocalDataSource.module
            )
            assertEquals(
                savedInput.status,
                inputFromLocalDataSource.status
            )
        }

    @Test
    fun `should save and delete input`() =
        runTest {
            // given an Input to save and delete
            val input = DummyInput().apply { id = 1234 }

            // when saving this Input
            inputLocalDataSource.saveInput(input)

            // when reading this Input from local data source
            val inputFromLocalDataSource = inputLocalDataSource.readInput(input.id)

            // then
            assertEquals(
                input.id,
                inputFromLocalDataSource.id
            )
            assertEquals(
                input.module,
                inputFromLocalDataSource.module
            )
            assertEquals(
                input.status,
                inputFromLocalDataSource.status
            )

            // when deleting this Input from local data source
            inputLocalDataSource.deleteInput(input.id)

            // then
            val exception = try {
                inputLocalDataSource.readInput(input.id)
            } catch (e: Exception) {
                e
            }

            assertTrue(exception is InputException.NotFoundException)
            assertEquals(
                (exception as InputException.NotFoundException).message,
                InputException.NotFoundException(input.id).message
            )
        }

    @Test
    fun `should save and export existing input`() =
        runTest {
            // given an Input to save and export
            val input = DummyInput().apply { id = 1234 }

            // when saving this Input
            inputLocalDataSource.saveInput(input)
            // and exporting this Input
            val exportedInput = inputLocalDataSource.exportInput(input.id)

            // when reading this Input from local data source
            val inputFromLocalDataSource = inputLocalDataSource.readInput(input.id)

            // then
            assertEquals(
                exportedInput.id,
                inputFromLocalDataSource.id
            )
            assertEquals(
                exportedInput.module,
                inputFromLocalDataSource.module
            )
            assertEquals(
                AbstractInput.Status.TO_SYNC,
                exportedInput.status
            )
            assertEquals(
                AbstractInput.Status.TO_SYNC,
                inputFromLocalDataSource.status
            )

            val exportedJsonFile = File(
                FileUtils.getInputsFolder(application),
                "input_${input.id}.json"
            )
            assertTrue(exportedJsonFile.exists())

            val inputFromExportedJsonFile = InputJsonReader(onInputJsonReaderListener).read(
                exportedJsonFile.bufferedReader()
            )
            assertEquals(
                input.id,
                inputFromExportedJsonFile.id
            )
            assertEquals(
                input.module,
                inputFromExportedJsonFile.module
            )
            assertEquals(
                AbstractInput.Status.TO_SYNC,
                inputFromExportedJsonFile.status
            )
        }

    @Test
    fun `should save an already exported input`() =
        runTest {
            // given an Input to save and export
            val input = DummyInput().apply { id = 1234 }

            // when saving this Input
            inputLocalDataSource.saveInput(input)
            // and exporting this Input
            val exportedInput = inputLocalDataSource.exportInput(input.id)

            // when reading this Input from local data source
            var inputFromLocalDataSource = inputLocalDataSource.readInput(input.id)

            // then
            assertEquals(
                exportedInput.id,
                inputFromLocalDataSource.id
            )
            assertEquals(
                AbstractInput.Status.TO_SYNC,
                exportedInput.status
            )
            assertEquals(
                AbstractInput.Status.TO_SYNC,
                inputFromLocalDataSource.status
            )

            var exportedJsonFile = File(
                FileUtils.getInputsFolder(application),
                "input_${input.id}.json"
            )
            assertTrue(exportedJsonFile.exists())

            // when editing again this exported Input
            inputLocalDataSource.saveInput(exportedInput)

            // and reading this Input from local data source
            inputFromLocalDataSource = inputLocalDataSource.readInput(input.id)

            // then
            assertEquals(
                exportedInput.id,
                inputFromLocalDataSource.id
            )
            assertEquals(
                AbstractInput.Status.DRAFT,
                inputFromLocalDataSource.status
            )

            exportedJsonFile = File(
                FileUtils.getInputsFolder(application),
                "input_${input.id}.json"
            )
            assertFalse(exportedJsonFile.exists())
        }

    @Test
    fun `should delete and existing exported input`() =
        runTest {
            // given an Input to save and export
            val input = DummyInput().apply { id = 1234 }

            // when saving this Input
            inputLocalDataSource.saveInput(input)
            // and exporting this Input
            val exportedInput = inputLocalDataSource.exportInput(input.id)

            // when reading this Input from local data source
            val inputFromLocalDataSource = inputLocalDataSource.readInput(input.id)

            // then
            assertEquals(
                exportedInput.id,
                inputFromLocalDataSource.id
            )
            assertEquals(
                exportedInput.module,
                inputFromLocalDataSource.module
            )
            assertEquals(
                AbstractInput.Status.TO_SYNC,
                exportedInput.status
            )
            assertEquals(
                AbstractInput.Status.TO_SYNC,
                inputFromLocalDataSource.status
            )
            assertTrue(
                File(
                    FileUtils.getInputsFolder(application),
                    "input_${input.id}.json"
                ).exists()
            )

            // when deleting this Input from local data source
            inputLocalDataSource.deleteInput(input.id)

            // then
            val exception = try {
                inputLocalDataSource.readInput(input.id)
            } catch (e: Exception) {
                e
            }

            assertTrue(exception is InputException.NotFoundException)
            assertEquals(
                (exception as InputException.NotFoundException).message,
                InputException.NotFoundException(input.id).message
            )

            assertFalse(
                File(
                    FileUtils.getInputsFolder(application),
                    "input_${input.id}.json"
                ).exists()
            )
        }

    @Test
    fun `should throw NotFoundException if trying to export undefined input`() =
        runTest {
            val exception = try {
                inputLocalDataSource.exportInput(1234)
            } catch (e: Exception) {
                e
            }

            assertTrue(exception is InputException.NotFoundException)
            assertEquals(
                (exception as InputException.NotFoundException).message,
                InputException.NotFoundException(1234).message
            )
        }
}