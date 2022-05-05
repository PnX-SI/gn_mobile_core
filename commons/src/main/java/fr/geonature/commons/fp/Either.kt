package fr.geonature.commons.fp

/**
 * Represents a value of one of two possible types (a disjoint union).
 * Instances of [Either] are either an instance of [Left] or [Right].
 * FP Convention dictates that [Left] is used for "failure"
 * and [Right] is used for "success".
 *
 * @see Left
 * @see Right
 */
sealed class Either<out L, out R> {

    /**
     * Represents the left side of [Either] class which by convention is a "Failure".
     */
    data class Left<out L>(val value: L) : Either<L, Nothing>()

    /**
     * Represents the right side of [Either] class which by convention is a "Success".
     */
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    /**
     * Returns true if this is a [Right], false otherwise.
     * @see Right
     */
    val isRight get() = this is Right<R>

    /**
     * Returns true if this is a [Left], false otherwise.
     * @see Left
     */
    val isLeft get() = this is Left<L>

    /**
     * Applies `fnL` if this is a [Left] or `fnR` if this is a [Right].
     *
     * Example:
     * ```kotlin
     * fun main() {
     *   fun possiblyFailingOperation(): Either.Right<Int> =
     *     Either.Right(1)
     *
     *   val result: Either<Exception, Int> = possiblyFailingOperation()
     *
     *   result.fold(
     *        { println("operation failed with $it") },
     *        { println("operation succeeded with $it") }
     *   )
     * }
     * ```
     *
     * @param fnL the function to apply if this is a [Left]
     * @param fnR the function to apply if this is a [Right]
     *
     * @see Left
     * @see Right
     */
    fun <C> fold(
        fnL: (L) -> C,
        fnR: (R) -> C
    ): C = when (this) {
        is Left -> fnL(value)
        is Right -> fnR(value)
    }
}

/**
 * Returns the right value if it exists, otherwise `null`.
 *
 * Example:
 * ```kotlin
 * val right = Right(12).orNull() // Result: 12
 * val left = Left(12).orNull()   // Result: null
 * ```
 */
fun <L, R> Either<L, R>.orNull(): R? = fold({ null }, { it })

/**
 * Right-biased map() FP convention which means that [Either.Right] is assumed to be the default case
 * to operate on.
 * If it is [Either.Left], operations like map, flatMap, ... return the [Either.Left] value unchanged.
 *
 * Example:
 * ```kotlin
 * val right = Either.Right(12).map { "flower" } // Result: Right("flower")
 * val left = Either.Left(12).map { "flower" }   // Result: Left(12)
 * ```
 */
fun <T, L, R> Either<L, R>.map(fn: (R) -> (T)): Either<L, T> = flatMap { Either.Right(fn(it)) }

/**
 * Right-biased flatMap() FP convention which means that [Either.Right] is assumed to be the default
 * case to operate on.
 * If it is [Either.Left], operations like map, flatMap, ... return the [Either.Left] value unchanged.
 */
fun <T, L, R> Either<L, R>.flatMap(fn: (R) -> Either<L, T>): Either<L, T> = when (this) {
    is Either.Left -> Either.Left(value)
    is Either.Right -> fn(value)
}

/**
 * Returns the value from this [Either.Right] or the given argument if this is a [Either.Left].
 *
 * Example:
 * ```kotlin
 * val right = Right(12).getOrElse { 17 } // Result: 12
 * val left = Left(12).getOrElse { 17 }   // Result: 17
 * ```
 */
fun <L, R> Either<L, R>.getOrElse(default: () -> R): R = fold(
    { default() },
    ::identity
)

/**
 * Returns the value from this [Either.Right] or allows to transform [Either.Left] to [Either.Right]
 * while providing access to the value of [Either.Left].
 *
 * Example:
 * ```kotlin
 * val right = Right(12).getOrHandle { 17 }   // Result: 12
 * val left = Left(12).getOrHandle { it + 5 } // Result: 17
 * ```
 */
fun <L, R> Either<L, R>.getOrHandle(default: (L) -> R): R = fold(
    { default(it) },
    ::identity
)

/**
 * Left-biased onFailure() FP convention dictates that when this class is [Either.Left], it'll perform
 * the onFailure functionality passed as a parameter, but, overall will still return an either
 * object so you chain calls.
 */
fun <L, R> Either<L, R>.onFailure(fn: (failure: L) -> Unit): Either<L, R> =
    apply { if (this is Either.Left) fn(value) }

/**
 * Right-biased onSuccess() FP convention dictates that when this class is [Either.Right], it'll perform
 * the onSuccess functionality passed as a parameter, but, overall will still return an either
 * object so you chain calls.
 */
fun <L, R> Either<L, R>.onSuccess(fn: (success: R) -> Unit): Either<L, R> =
    apply { if (this is Either.Right) fn(value) }