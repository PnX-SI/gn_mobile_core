package fr.geonature.commons.features.input.usecase

import fr.geonature.commons.error.Failure
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.repository.IInputRepository
import fr.geonature.commons.fp.Either
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.commons.settings.IAppSettings

/**
 * Reads all [AbstractInput]s.
 *
 * @author S. Grimault
 */
open class ReadInputsUseCase<I : AbstractInput, S : IAppSettings>(private val inputRepository: IInputRepository<I, S>) :
    BaseUseCase<List<I>, BaseUseCase.None>() {

    override suspend fun run(params: None): Either<Failure, List<I>> {
        return inputRepository.readInputs()
    }
}