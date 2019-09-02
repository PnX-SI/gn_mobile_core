package fr.geonature.commons.input

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.MainApplication
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
    private lateinit var inputManager: InputManager<DummyInput>

    @Mock
    private lateinit var onInputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<DummyInput>

    @Mock
    private lateinit var onInputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<DummyInput>

    @Mock
    private lateinit var application: MainApplication

    private lateinit var inputViewModel: DummyInputViewModel


    @Before
    fun setUp() {
        initMocks(this)

        inputViewModel = spy(DummyInputViewModel(application,
                                                 onInputJsonReaderListener,
                                                 onInputJsonWriterListener))
        doReturn(inputManager).`when`(inputViewModel)
            .inputManager
    }

    @Test
    fun testCreateFromFactory() {
        // given Factory
        val factory = InputViewModel.Factory {
            DummyInputViewModel(application,
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
            doReturn(emptyList<DummyInput>()).`when`(inputManager)
                .readInputs()

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
            doReturn(existingInputs).`when`(inputManager)
                .readInputs()

            // then
            inputViewModel.readInputs()
                .observeOnce { inputs ->
                    assertNotNull(inputs)
                    assertArrayEquals(existingInputs.map { it.id }.toTypedArray(),
                                      requireNotNull(inputs).map { it.id }.toTypedArray())
                }
        }
    }

    class DummyInputViewModel(application: MainApplication,
                              inputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<DummyInput>,
                              inputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<DummyInput>) : InputViewModel<DummyInput>(application,
                                                                                                                                           inputJsonReaderListener,
                                                                                                                                           inputJsonWriterListener)
}