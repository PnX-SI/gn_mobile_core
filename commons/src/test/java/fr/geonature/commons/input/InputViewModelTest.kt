package fr.geonature.commons.input

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.input.io.InputJsonWriter
import fr.geonature.commons.observeOnce
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.MockitoAnnotations.initMocks
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputViewModel].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var onInputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<DummyInput>

    @Mock
    private lateinit var onInputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<DummyInput>

    private lateinit var inputViewModel: DummyInputViewModel

    @Before
    fun setUp() {
        initMocks(this)

        doReturn(DummyInput()).`when`(onInputJsonReaderListener)
            .createInput()

        inputViewModel = spy(DummyInputViewModel(ApplicationProvider.getApplicationContext(),
                                                 onInputJsonReaderListener,
                                                 onInputJsonWriterListener))
    }

    @Test
    fun testCreateFromFactory() {
        // given Factory
        val factory = InputViewModel.Factory {
            DummyInputViewModel(ApplicationProvider.getApplicationContext(),
                                onInputJsonReaderListener,
                                onInputJsonWriterListener)
        }

        // when create InputViewModel instance from this factory
        val viewModelFromFactory = factory.create(DummyInputViewModel::class.java)

        // then
        assertNotNull(viewModelFromFactory)
    }

    @Test
    fun testReadUndefinedInputs() {
        runBlocking {
            inputViewModel.readInputs()
                .observeOnce {
                    assertNotNull(it)
                    assertTrue(requireNotNull(it).isEmpty())
                }
        }
    }

    @Test
    fun testReadExistingInputs() {
        runBlocking {
            // given existing inputs
            val existingInputs = listOf(DummyInput().apply { id = 1234 },
                                        DummyInput().apply { id = 1235 },
                                        DummyInput().apply { id = 1236 })
            existingInputs.forEach { inputViewModel.inputManager.saveInput(it) }

            // then
            inputViewModel.readInputs()
                .observeOnce { inputs ->
                    assertNotNull(inputs)
                    assertArrayEquals(existingInputs.map { it.id }.toTypedArray(),
                                      requireNotNull(inputs).map { it.id }.toTypedArray())
                }
        }
    }

    @Test
    fun testSaveInput() {
        runBlocking {
            // given existing inputs
            val existingInputs = listOf(DummyInput().apply { id = 1234 },
                                        DummyInput().apply { id = 1235 },
                                        DummyInput().apply { id = 1236 })
            existingInputs.forEach { inputViewModel.inputManager.saveInput(it) }

            // when adding new input
            inputViewModel.saveInput(DummyInput().apply { id = 1237 })

            //then
            inputViewModel.readInputs()
                .observeOnce { inputs ->
                    assertNotNull(inputs)
                    assertArrayEquals(arrayOf(*existingInputs.map { it.id }.toTypedArray(),
                                              1237),
                                      requireNotNull(inputs).map { it.id }.toTypedArray())
                }
        }
    }

    @Test
    fun testDeleteInput() {
        runBlocking {
            // given existing inputs
            val existingInputs = listOf(DummyInput().apply { id = 1234 },
                                        DummyInput().apply { id = 1235 },
                                        DummyInput().apply { id = 1236 })
            existingInputs.forEach { inputViewModel.inputManager.saveInput(it) }

            // when deleting input
            inputViewModel.deleteInput(existingInputs[1])

            // then
            inputViewModel.readInputs()
                .observeOnce { inputs ->
                    assertNotNull(inputs)
                    assertArrayEquals(existingInputs.filter { it.id != 1235L }.map { it.id }.toTypedArray(),
                                      requireNotNull(inputs).map { it.id }.toTypedArray())
                }
        }
    }

    @Test
    fun testDeleteAndRestoreInput() {
        runBlocking {
            // given existing inputs
            val existingInputs = listOf(DummyInput().apply { id = 1234 },
                                        DummyInput().apply { id = 1235 },
                                        DummyInput().apply { id = 1236 })
            existingInputs.forEach { inputViewModel.inputManager.saveInput(it) }

            // when deleting input
            inputViewModel.deleteInput(existingInputs[1])
            // and restoring previously deleted input
            inputViewModel.restoreDeletedInput()

            // then
            inputViewModel.readInputs()
                .observeOnce { inputs ->
                    assertNotNull(inputs)
                    assertArrayEquals(existingInputs.map { it.id }.toTypedArray(),
                                      requireNotNull(inputs).map { it.id }.toTypedArray())
                }
        }
    }

    class DummyInputViewModel(application: Application,
                              inputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<DummyInput>,
                              inputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<DummyInput>) : InputViewModel<DummyInput>(application,
                                                                                                                                           inputJsonReaderListener,
                                                                                                                                           inputJsonWriterListener)
}