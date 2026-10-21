package fr.geonature.datasync.util

import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * Unit tests about [ListenableFuture.awaitCompat].
 *
 * @author S. Grimault
 */
class ListenableFutureHelperTest {

    @Test
    fun `should await an already completed future`() = runTest {
        val future = TestListenableFuture<String>().apply { set("some_result") }

        assertEquals(
            "some_result",
            future.awaitCompat()
        )
    }

    @Test
    fun `should await a future completed after being subscribed`() = runTest {
        val future = TestListenableFuture<String>()

        val job = launch {
            assertEquals(
                "some_result",
                future.awaitCompat()
            )
        }

        future.set("some_result")
        job.join()
    }

    @Test
    fun `should rethrow the cause of a failed future`() = runTest {
        val expectedFailure = RuntimeException("something went wrong")
        val future = TestListenableFuture<String>().apply { setException(expectedFailure) }

        val failure = runCatching { future.awaitCompat() }.exceptionOrNull()

        // note: kotlinx.coroutines may recover the propagated exception with a copy of it
        // (same type and message) across suspension points, so comparing messages here
        // instead of the exception instance itself
        assertEquals(
            expectedFailure.javaClass,
            failure?.javaClass
        )
        assertEquals(
            expectedFailure.message,
            failure?.message
        )
    }

    @Test
    fun `should rethrow the exception thrown by get as is if it has no cause`() = runTest {
        val future = TestListenableFuture<String>().apply { cancel(false) }

        val failure = runCatching { future.awaitCompat() }.exceptionOrNull()

        assertTrue(failure is java.util.concurrent.CancellationException)
    }

    @Test
    fun `should cancel the underlying future if the calling coroutine is cancelled`() = runTest {
        val future = TestListenableFuture<String>()

        val job = launch {
            runCatching { future.awaitCompat() }
        }
        testScheduler.runCurrent()

        assertTrue(!future.isCancelled)

        job.cancel(CancellationException("cancelled by test"))
        job.join()

        assertTrue(future.isCancelled)
    }

    /**
     * Minimal [ListenableFuture] test double, completing (or cancelling) its registered listener
     * synchronously, similar to real [ListenableFuture] implementations (e.g. Guava's
     * `AbstractFuture`).
     */
    private class TestListenableFuture<T> : ListenableFuture<T> {
        private var listener: Runnable? = null
        private var executor: Executor? = null
        private var result: Result<T>? = null
        private var cancelled = false

        fun set(value: T) {
            result = Result.success(value)
            notifyListener()
        }

        fun setException(throwable: Throwable) {
            result = Result.failure(throwable)
            notifyListener()
        }

        override fun addListener(
            listener: Runnable,
            executor: Executor
        ) {
            this.listener = listener
            this.executor = executor

            if (isDone) {
                notifyListener()
            }
        }

        override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
            if (isDone) return false

            cancelled = true
            notifyListener()

            return true
        }

        override fun isCancelled(): Boolean = cancelled

        override fun isDone(): Boolean = cancelled || result != null

        override fun get(): T {
            if (cancelled) throw java.util.concurrent.CancellationException()

            return result
                ?.getOrElse { throw ExecutionException(it) }
                ?: throw IllegalStateException("future not completed")
        }

        override fun get(
            timeout: Long,
            unit: TimeUnit
        ): T = get()

        private fun notifyListener() {
            listener?.also { runnable -> executor?.execute(runnable) }
        }
    }
}




