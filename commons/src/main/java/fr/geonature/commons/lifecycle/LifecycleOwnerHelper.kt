package fr.geonature.commons.lifecycle

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import fr.geonature.commons.error.Failure

/**
 * Function helpers for `LifecycleOwner` utilities.
 *
 * @author S. Grimault
 */

fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(
    liveData: L,
    body: (T) -> Unit
) = liveData.observe(
    this,
    Observer(body)
)

fun <L : LiveData<Failure>> LifecycleOwner.onFailure(
    liveData: L,
    body: (Failure) -> Unit
) = liveData.observe(
    this,
    Observer(body)
)

fun <L : LiveData<Throwable>> LifecycleOwner.onError(
    liveData: L,
    body: (Throwable) -> Unit
) = liveData.observe(
    this,
    Observer(body)
)