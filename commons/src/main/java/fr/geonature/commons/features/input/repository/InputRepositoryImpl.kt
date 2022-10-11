package fr.geonature.commons.features.input.repository

import fr.geonature.commons.error.Failure
import fr.geonature.commons.features.input.data.IInputLocalDataSource
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.error.InputException
import fr.geonature.commons.features.input.error.InputFailure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.settings.IAppSettings

/**
 * Default implementation of [IInputRepository].
 *
 * @author S. Grimault
 */
class InputRepositoryImpl<I : AbstractInput, S : IAppSettings>(private val inputLocalDataSource: IInputLocalDataSource<I, S>) :
    IInputRepository<I, S> {

    override suspend fun readInputs(): Either<Failure, List<I>> {
        return runCatching { inputLocalDataSource.readInputs() }.fold(
            onSuccess = { Either.Right(it) },
            onFailure = { return Either.Left(throwableToFailure(it)) },
        )
    }

    override suspend fun readInput(id: Long): Either<Failure, I> {
        return runCatching { inputLocalDataSource.readInput(id) }.fold(
            onSuccess = { Either.Right(it) },
            onFailure = { return Either.Left(throwableToFailure(it)) },
        )
    }

    override suspend fun saveInput(input: I): Either<Failure, I> {
        return runCatching { inputLocalDataSource.saveInput(input) }.fold(
            onSuccess = { Either.Right(it) },
            onFailure = { return Either.Left(throwableToFailure(it)) },
        )
    }

    override suspend fun deleteInput(id: Long): Either<Failure, Unit> {
        return runCatching { inputLocalDataSource.deleteInput(id) }.fold(
            onSuccess = { Either.Right(it) },
            onFailure = { return Either.Left(throwableToFailure(it)) },
        )
    }

    override suspend fun exportInput(
        id: Long,
        settings: S?
    ): Either<Failure, I> {
        return runCatching {
            inputLocalDataSource.exportInput(
                id,
                settings
            )
        }.fold(
            onSuccess = { Either.Right(it) },
            onFailure = { return Either.Left(throwableToFailure(it)) },
        )
    }

    override suspend fun exportInput(
        input: I,
        settings: S?
    ): Either<Failure, I> {
        return runCatching {
            inputLocalDataSource.exportInput(
                input,
                settings
            )
        }.fold(
            onSuccess = { Either.Right(it) },
            onFailure = { return Either.Left(throwableToFailure(it)) },
        )
    }

    private fun throwableToFailure(throwable: Throwable): Failure {
        return when (throwable) {
            is InputException.NotFoundException -> InputFailure.NotFoundFailure(
                throwable.message
                    ?: "no input found with ID '${throwable.id}'"
            )
            is InputException.ReadException -> InputFailure.IOFailure(
                throwable.message
                    ?: "I/O error while reading input with ID '${throwable.id}'"
            )
            is InputException.WriteException -> InputFailure.IOFailure(
                throwable.message
                    ?: "I/O error while writing input with ID '${throwable.id}'"
            )
            else -> InputFailure.Failure
        }
    }
}