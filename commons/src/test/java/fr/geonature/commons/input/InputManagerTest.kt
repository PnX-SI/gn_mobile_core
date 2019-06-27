package fr.geonature.commons.input

import android.app.Application
import android.util.JsonReader
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.input.io.InputJsonWriter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputManager].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputManagerTest {

    private lateinit var inputManager: InputManager<DummyInput>

    @Mock
    private lateinit var onInputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<DummyInput>

    private lateinit var onInputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<DummyInput>

    @Before
    fun setUp() {
        initMocks(this)

        onInputJsonReaderListener = object : InputJsonReader.OnInputJsonReaderListener<DummyInput> {
            override fun createInput(): DummyInput {
                return DummyInput()
            }

            override fun readAdditionalInputData(reader: JsonReader,
                                                 keyName: String,
                                                 input: DummyInput) {
            }
        }

        val application = getApplicationContext<Application>()
        inputManager = InputManager(application,
                                    onInputJsonReaderListener,
                                    onInputJsonWriterListener)
    }

    @Test
    fun testReadUndefinedInputs() {
        // when reading non existing inputs
        val noSuchInputs = runBlocking { inputManager.readInputs() }

        // then
        assertTrue(noSuchInputs.isEmpty())
    }

    @Test
    fun testSaveAndReadInputs() {
        // given some inputs to save and read
        val input1 = DummyInput().apply { id = 1234 }
        val input2 = DummyInput().apply { id = 1235 }
        val input3 = DummyInput().apply { id = 1236 }

        val saved = runBlocking {
            val s1 = inputManager.saveInput(input1)
            val s2 = inputManager.saveInput(input2)
            val s3 = inputManager.saveInput(input3)

            s1 && s2 && s3
        }

        // then
        assertTrue(saved)

        // when reading these inputs from manager
        val inputs = runBlocking { inputManager.readInputs() }

        // then
        assertArrayEquals(arrayOf(input1.id,
                                  input2.id,
                                  input3.id),
                          inputs.map { it.id }.toTypedArray())
    }

    @Test
    fun testReadUndefinedInput() {
        // when reading non existing Input
        val noSuchInput = runBlocking { inputManager.readInput(1234) }

        // then
        assertNull(noSuchInput)
    }

    @Test
    fun testSaveAndReadInput() {
        // given an Input to save and read
        val input = DummyInput().apply { id = 1234 }

        // when saving this Input
        val saved = runBlocking { inputManager.saveInput(input) }

        // then
        assertTrue(saved)

        // when reading this Input from manager
        val readInput = runBlocking { inputManager.readInput(input.id) }

        // then
        assertNotNull(readInput)
        assertEquals(input.id,
                     readInput!!.id)
        assertEquals(input.module,
                     readInput.module)

        // when reading this Input as default Input from manager
        val defaultInput = runBlocking { inputManager.readInput() }

        // then
        assertNotNull(defaultInput)
        assertEquals(input.id,
                     defaultInput!!.id)
        assertEquals(input.module,
                     defaultInput.module)

        // when reading this Input as current Input from manager
        val currentInput = runBlocking { inputManager.readCurrentInput() }

        // then
        assertNotNull(currentInput)
        assertEquals(input.id,
                     currentInput!!.id)
        assertEquals(input.module,
                     currentInput.module)
    }

    @Test
    fun testSaveAndDeleteInput() {
        // given an Input to save and delete
        val input = DummyInput().apply { id = 1234 }

        // when saving this Input
        val saved = runBlocking { inputManager.saveInput(input) }

        // then
        assertTrue(saved)

        // when deleting this Input from manager
        val deleted = runBlocking { inputManager.deleteInput(input.id) }

        // then
        assertTrue(deleted)
        val noSuchInput = runBlocking { inputManager.readInput(input.id) }

        assertNull(noSuchInput)
    }

    @Test
    fun testExportUndefinedInput() {
        // when exporting non existing Input
        val exported = runBlocking { inputManager.exportInput(1234) }

        // then
        assertFalse(exported)
    }

    @Test
    fun testSaveAndExportInput() {
        // given an Input to save and export
        val input = DummyInput().apply { id = 1234 }

        // when saving this Input
        val saved = runBlocking { inputManager.saveInput(input) }
        // and exporting this Input
        val exported = runBlocking { inputManager.exportInput(input.id) }

        // then
        assertTrue(saved)
        assertTrue(exported)
    }
}