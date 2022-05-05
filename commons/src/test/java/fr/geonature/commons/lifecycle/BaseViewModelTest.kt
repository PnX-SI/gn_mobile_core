package fr.geonature.commons.lifecycle

import androidx.lifecycle.Observer
import fr.geonature.commons.error.Failure
import io.mockk.MockKAnnotations.init
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [BaseViewModel].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class BaseViewModelTest {

    private lateinit var viewModel: DummyViewModel

    @RelaxedMockK
    private lateinit var failureObserver: Observer<Failure>

    @Before
    fun setUp() {
        init(this)

        viewModel = DummyViewModel()
    }

    @Test
    fun `should handle failure by updating live data`() = runTest {
        viewModel.handleError(Failure.NetworkFailure(reason = "not_connected"))
        viewModel.failure.observeForever(failureObserver)

        // then
        verify(atLeast = 1) { failureObserver.onChanged(Failure.NetworkFailure(reason = "not_connected")) }
    }

    private class DummyViewModel : BaseViewModel() {
        fun handleError(failure: Failure) = handleFailure(failure)
    }
}