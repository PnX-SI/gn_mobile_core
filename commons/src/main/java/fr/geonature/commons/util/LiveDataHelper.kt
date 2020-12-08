package fr.geonature.commons.util

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
    observe(
        owner,
        object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        }
    )
}

/**
 * Adds the given observer to the observers list within the lifespan of the given owner.
 * The observer will only receive events until given condition is met.
 */
fun <T> LiveData<T>.observeUntil(
    owner: LifecycleOwner,
    until: (T?) -> Boolean,
    observer: (T?) -> Unit
) {
    observe(
        owner,
        object : Observer<T> {
            override fun onChanged(value: T) {
                observer(value)

                if (until(value)) {
                    removeObserver(this)
                }
            }
        }
    )
}
