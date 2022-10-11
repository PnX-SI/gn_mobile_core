package fr.geonature.commons.features.input.usecase

import fr.geonature.commons.error.Failure
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.repository.IInputRepository
import fr.geonature.commons.fp.Either
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.commons.settings.IAppSettings

/**
 * Exports the given [AbstractInput].
 *
 * @author S. Grimault
 */
open class ExportInputUseCase<I : AbstractInput, S : IAppSettings>(private val inputRepository: IInputRepository<I, S>) :
    BaseUseCase<I, ExportInputUseCase.Params<I, S>>() {

    override suspend fun run(params: Params<I, S>): Either<Failure, I> {
        return inputRepository.exportInput(
            params.input,
            params.settings
        )
    }

    data class Params<I, S>(
        val input: I,
        val settings: S? = null
    )
}