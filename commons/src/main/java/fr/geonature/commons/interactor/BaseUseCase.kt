package fr.geonature.commons.interactor

import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Abstract class for a Use Case (_Interactor_ in terms of Clean Architecture).
 * This abstraction represents an execution unit for different use cases (this means that any use
 * case in the application should implement this contract).
 *
 * By convention each [BaseUseCase] implementation will execute its job in a background thread
 * (kotlin coroutine) and will post the result in the UI thread.
 */
abstract class BaseUseCase<out Type, in Params> where Type : Any {

    abstract suspend fun run(params: Params): Either<Failure, Type>

    operator fun invoke(
        params: Params,
        scope: CoroutineScope,
        onResult: (Either<Failure, Type>) -> Unit = {}
    ) {
        scope.launch {
            onResult(run(params))
        }
    }

    /**
     * No parameters.
     */
    class None
}

/**
 * Abstract class for a Use Case (_Interactor_ in terms of Clean Architecture).
 * This abstraction represents an execution unit for different use cases (this means that any use
 * case in the application should implement this contract).
 *
 * By convention each [BaseResultUseCase] implementation will execute its job in a background thread
 * (kotlin coroutine) and will post the result in the UI thread.
 */
abstract class BaseResultUseCase<out Type, in Params> where Type : Any {

    abstract suspend fun run(params: Params): Result<Type>

    operator fun invoke(
        params: Params,
        scope: CoroutineScope,
        onResult: (Result<Type>) -> Unit = {}
    ) {
        scope.launch {
            onResult(run(params))
        }
    }

    /**
     * No parameters.
     */
    class None
}

/**
 * Abstract class for a Use Case (_Interactor_ in terms of Clean Architecture).
 * This abstraction represents an execution unit for different use cases (this means that any use
 * case in the application should implement this contract).
 *
 * By convention each [BaseFlowUseCase] implementation will execute its job in a background thread
 * (kotlin coroutine) and will post a flow result in the UI thread.
 */
abstract class BaseFlowUseCase<out Type, in Params> where Type : Any {

    abstract suspend fun run(params: Params): Flow<Type>

    operator fun invoke(
        params: Params,
        scope: CoroutineScope,
        onResult: (Flow<Type>) -> Unit = {}
    ) {
        scope.launch {
            onResult(run(params))
        }
    }

    /**
     * No parameters.
     */
    class None
}