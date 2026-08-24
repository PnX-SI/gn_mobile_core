package fr.geonature.datasync.util

import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * `ListenableFuture` helpers.
 *
 * @author S. Grimault
 */

/**
 * Awaits the result of this [ListenableFuture] without relying on the `androidx.work.await`
 * extension which is restricted to the `androidx.work` library group and cannot be used
 * from this module.
 */
suspend fun <T> ListenableFuture<T>.awaitCompat(): T =
    suspendCancellableCoroutine { continuation ->
        addListener(
            {
                val result = runCatching { get() }
                result.fold(
                    onSuccess = { continuation.resume(it) },
                    onFailure = { continuation.resumeWithException(it.cause ?: it) }
                )
            },
            { it.run() }
        )

        continuation.invokeOnCancellation { cancel(false) }
    }