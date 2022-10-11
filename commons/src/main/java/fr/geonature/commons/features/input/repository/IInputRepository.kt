package fr.geonature.commons.features.input.repository

import fr.geonature.commons.error.Failure
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.fp.Either
import fr.geonature.commons.settings.IAppSettings

/**
 * [AbstractInput] repository.
 *
 * @author S. Grimault
 */
interface IInputRepository<I : AbstractInput, S : IAppSettings> {

    /**
     * Reads all [AbstractInput]s.
     *
     * @return A list of [AbstractInput]s
     */
    suspend fun readInputs(): Either<Failure, List<I>>

    /**
     * Reads [AbstractInput] from a given ID.
     *
     * @param id The [AbstractInput] ID to read.
     *
     * @return [AbstractInput] or [Failure] if not found or something goes wrong
     */
    suspend fun readInput(id: Long): Either<Failure, I>

    /**
     * Saves the given [AbstractInput].
     *
     * @param input the [AbstractInput] to save
     */
    suspend fun saveInput(input: I): Either<Failure, I>

    /**
     * Deletes [AbstractInput] from a given ID.
     *
     * @param id the [AbstractInput] ID to delete
     */
    suspend fun deleteInput(id: Long): Either<Failure, Unit>

    /**
     * Exports [AbstractInput] from a given ID as `JSON` file.
     *
     * @param id the [AbstractInput] ID to export
     * @param settings additional settings
     */
    suspend fun exportInput(
        id: Long,
        settings: S? = null
    ): Either<Failure, I>

    /**
     * Exports [AbstractInput] as `JSON` file.
     *
     * @param input the [AbstractInput] to save
     * @param settings additional settings
     */
    suspend fun exportInput(
        input: I,
        settings: S? = null
    ): Either<Failure, I>
}