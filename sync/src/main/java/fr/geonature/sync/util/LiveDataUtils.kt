package fr.geonature.sync.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Helpers for [LiveData] utilities.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */

/**
 * Adds the given observer to the observers list within the lifespan of the given owner.
 * The observer will only receive a single event and will automatically be removed.
 */
fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T?) -> Unit) {
    observe(owner, object: Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            observer(value)
        }
    })
}