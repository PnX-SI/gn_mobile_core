package fr.geonature.commons.input

import android.app.Application
import android.content.pm.ProviderInfo
import android.util.JsonReader
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.commons.data.DummyContentProvider
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.atMost
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.openMocks
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputManagerImpl].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputManagerTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var inputManager: InputManagerImpl<DummyInput>

    @Mock
    private lateinit var onInputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<DummyInput>

    private lateinit var onInputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<DummyInput>

    @Mock
    private lateinit var observerForListOfInputs: Observer<List<DummyInput>>

    @Mock
    private lateinit var observerForInput: Observer<DummyInput?>

    @Before
    fun setUp() {
        openMocks(this)

        onInputJsonReaderListener = object : InputJsonReader.OnInputJsonReaderListener<DummyInput> {
            override fun createInput(): DummyInput {
                return DummyInput()
            }

            override fun readAdditionalInputData(
                reader: JsonReader,
                keyName: String,
                input: DummyInput
            ) {
            }
        }

        val application = getApplicationContext<Application>()

        val info = ProviderInfo()
        info.authority = "fr.geonature.sync.provider"
        info.grantUriPermissions = true
        Robolectric
            .buildContentProvider(DummyContentProvider::class.java)
            .create(info)

        inputManager = InputManagerImpl(
            application,
            info.authority,
            onInputJsonReaderListener,
            onInputJsonWriterListener
        )
        inputManager.inputs.observeForever(observerForListOfInputs)
        inputManager.input.observeForever(observerForInput)

        PreferenceManager
            .getDefaultSharedPreferences(application)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun testReadUndefinedInputs() { // when reading non existing inputs
        val noSuchInputs = runBlocking { inputManager.readInputs() }

        // then
        assertTrue(noSuchInputs.isEmpty())
    }

    @Test
    fun testSaveAndReadInputs() { // given some inputs to save and read
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
        assertArrayEquals(arrayOf(
            input1.id,
            input2.id,
            input3.id
        ),
            inputs
                .map { it.id }
                .toTypedArray())

        verify(observerForListOfInputs).onChanged(inputs)
    }

    @Test
    fun testReadUndefinedInput() { // when reading non existing Input
        val noSuchInput = runBlocking { inputManager.readInput(1234) }

        // then
        assertNull(noSuchInput)
    }

    @Test
    fun testSaveAndReadInput() { // given an Input to save and read
        val input = DummyInput().apply { id = 1234 }

        // when saving this Input
        val saved = runBlocking { inputManager.saveInput(input) }

        // then
        assertTrue(saved)

        // when reading this Input from manager
        val readInput = runBlocking { inputManager.readInput(input.id) }

        // then
        assertNotNull(readInput)
        assertEquals(
            input.id,
            readInput!!.id
        )
        assertEquals(
            input.module,
            readInput.module
        )

        // when reading this Input as default Input from manager
        val defaultInput = runBlocking { inputManager.readInput() }

        // then
        assertNotNull(defaultInput)
        assertEquals(
            input.id,
            defaultInput!!.id
        )
        assertEquals(
            input.module,
            defaultInput.module
        )

        // when reading this Input as current Input from manager
        val currentInput = runBlocking { inputManager.readCurrentInput() }

        // then
        assertNotNull(currentInput)
        assertEquals(
            input.id,
            currentInput!!.id
        )
        assertEquals(
            input.module,
            currentInput.module
        )

        verify(
            observerForInput,
            atMost(2)
        ).onChanged(readInput)
    }

    @Test
    fun testSaveAndDeleteInput() { // given an Input to save and delete
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

        verify(
            observerForInput,
            atMost(2)
        ).onChanged(null)
    }

    @Test
    fun testExportUndefinedInput() { // when exporting non existing Input
        val exported = runBlocking { inputManager.exportInput(1234) }

        // then
        assertFalse(exported)
    }

    @Test
    fun testSaveAndExportExistingInput() { // given an Input to save and export
        val input = DummyInput().apply { id = 1234 }

        // when saving this Input
        val saved = runBlocking { inputManager.saveInput(input) } // and exporting this Input
        val exported = runBlocking { inputManager.exportInput(input.id) }

        // then
        assertTrue(saved)
        assertTrue(exported)

        val noSuchInput = runBlocking { inputManager.readInput(input.id) }
        assertNull(noSuchInput)

        verify(
            observerForListOfInputs,
            atMost(2)
        ).onChanged(emptyList())
        verify(
            observerForInput,
            atMost(2)
        ).onChanged(null)
    }

    @Test
    fun testSaveAndExportInput() { // given an Input to save and export
        val input = DummyInput().apply { id = 1234 }

        // when exporting this Input
        val exported = runBlocking { inputManager.exportInput(input) }

        // then
        assertTrue(exported)

        val noSuchInput = runBlocking { inputManager.readInput(input.id) }
        assertNull(noSuchInput)

        verify(observerForListOfInputs).onChanged(emptyList())
        verify(observerForInput).onChanged(null)
    }
}
