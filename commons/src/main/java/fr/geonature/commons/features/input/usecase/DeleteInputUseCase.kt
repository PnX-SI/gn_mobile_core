package fr.geonature.commons.features.input.usecase

import fr.geonature.commons.error.Failure
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.repository.IInputRepository
import fr.geonature.commons.fp.Either
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.commons.settings.IAppSettings

/**
 * Deletes given [AbstractInput].
 *
 * @author S. Grimault
 */
open class DeleteInputUseCase<I : AbstractInput, S : IAppSettings>(private val inputRepository: IInputRepository<I, S>) :
    BaseUseCase<Unit, DeleteInputUseCase.Params<I>>() {

    override suspend fun run(params: Params<I>): Either<Failure, Unit> {
        return inputRepository.deleteInput(params.input.id)
    }

    data class Params<I>(val input: I)
}