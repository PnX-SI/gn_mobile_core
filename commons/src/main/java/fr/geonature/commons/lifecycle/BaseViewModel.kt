package fr.geonature.commons.lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fr.geonature.commons.error.Failure

/**
 * Base `ViewModel` class with default [Failure] handling.
 *
 * @see ViewModel
 * @see Failure
 */
abstract class BaseViewModel : ViewModel() {

    private val _failure: MutableLiveData<Failure> = MutableLiveData()
    val failure: LiveData<Failure> = _failure

    private val _error: MutableLiveData<Throwable> = MutableLiveData()
    val error: LiveData<Throwable> = _error

    protected fun handleFailure(failure: Failure) {
        _failure.value = failure
    }

    protected fun handleError(error: Throwable) {
        _error.value = error
    }
}