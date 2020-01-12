package fr.geonature.commons.input

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.input.io.InputJsonWriter
import org.junit.Assert.assertNotNull
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

        inputViewModel = spy(
            DummyInputViewModel(
                ApplicationProvider.getApplicationContext(),
                onInputJsonReaderListener,
                onInputJsonWriterListener
            )
        )
    }

    @Test
    fun testCreateFromFactory() {
        // given Factory
        val factory = InputViewModel.Factory {
            DummyInputViewModel(
                ApplicationProvider.getApplicationContext(),
                onInputJsonReaderListener,
                onInputJsonWriterListener
            )
        }

        // when create InputViewModel instance from this factory
        val viewModelFromFactory = factory.create(DummyInputViewModel::class.java)

        // then
        assertNotNull(viewModelFromFactory)
    }

    class DummyInputViewModel(
        application: Application,
        inputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<DummyInput>,
        inputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<DummyInput>
    ) : InputViewModel<DummyInput>(
        application,
        inputJsonReaderListener,
        inputJsonWriterListener
    )
}
