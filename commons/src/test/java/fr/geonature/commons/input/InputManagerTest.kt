package fr.geonature.commons.input

import android.app.Application
import android.util.JsonReader
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.commons.MockitoKotlinHelper.any
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.input.io.InputJsonWriter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doReturn
import org.mockito.MockitoAnnotations.initMocks
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputManager].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputManagerTest {

    private lateinit var inputManager: InputManager

    @Mock
    private lateinit var onInputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener

    @Mock
    private lateinit var onInputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener

    @Before
    fun setUp() {
        initMocks(this)

        doReturn(DummyInput()).`when`(onInputJsonReaderListener)
            .createInput()
        `when`(onInputJsonReaderListener.readAdditionalInputData(any(JsonReader::class.java),
                                                                 any(String::class.java),
                                                                 any(DummyInput::class.java))).then {
            (it.getArgument(0) as JsonReader).skipValue()
        }

        val application = getApplicationContext<Application>()
        inputManager = InputManager(application,
                                    onInputJsonReaderListener,
                                    onInputJsonWriterListener)
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