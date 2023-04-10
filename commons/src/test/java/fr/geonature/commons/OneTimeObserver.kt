package fr.geonature.commons

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Observer implementation that owns its lifecycle and achieves a one-time only observation
 * by marking it as destroyed once the `onChange` handler is executed.
 *
 * @param handler the handler to execute on change.
 *
 * @author S. Grimault
 */
class OneTimeObserver<T>(private val handler: (T?) -> Unit) : Observer<T>, LifecycleOwner {
    override val lifecycle = LifecycleRegistry(this)

    init {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onChanged(value: T) {
        handler(value)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

fun <T> LiveData<T>.observeOnce(onChangeHandler: (T?) -> Unit) {
    val observer = OneTimeObserver(handler = onChangeHandler)
    observe(
        observer,
        observer
    )
}
