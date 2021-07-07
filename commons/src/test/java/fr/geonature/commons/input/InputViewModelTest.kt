package fr.geonature.commons.input

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
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
    private lateinit var inputManager: IInputManager<DummyInput>

    private lateinit var inputViewModel: InputViewModel<DummyInput>

    @Before
    fun setUp() {
        initMocks(this)

        inputViewModel = spy(InputViewModel(inputManager))
    }

    @Test
    fun testCreateFromFactory() { // given Factory
        val factory = InputViewModel.Factory {
            InputViewModel(inputManager)
        }

        // when create InputViewModel instance from this factory
        val viewModelFromFactory = factory.create(InputViewModel::class.java)

        // then
        assertNotNull(viewModelFromFactory)
    }
}
