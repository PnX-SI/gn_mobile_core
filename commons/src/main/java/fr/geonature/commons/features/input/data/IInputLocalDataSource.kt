package fr.geonature.commons.features.input.data

import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.error.InputException
import fr.geonature.commons.settings.IAppSettings

/**
 * Local data source about [AbstractInput].
 *
 * @author S. Grimault
 */
interface IInputLocalDataSource<I : AbstractInput, S : IAppSettings> {

    /**
     * Reads all [AbstractInput]s, both those in progress and those ready to be synchronized.
     *
     * @return A list of [AbstractInput]s
     */
    suspend fun readInputs(): List<I>

    /**
     * Reads [AbstractInput] from a given ID.
     *
     * @param id The [AbstractInput] ID to read.
     *
     * @return [AbstractInput] or throws [InputException.NotFoundException] if not found
     *
     * @throws [InputException.NotFoundException] it not found
     * @throws [InputException.ReadException] if something goes wrong
     */
    suspend fun readInput(id: Long): I

    /**
     * Saves the given [AbstractInput].
     *
     * @param input the [AbstractInput] to save
     *
     * @return [AbstractInput] saved
     *
     * @throws [InputException.WriteException] if something goes wrong
     */
    suspend fun saveInput(
        input: I,
        status: AbstractInput.Status = AbstractInput.Status.DRAFT
    ): I

    /**
     * Deletes [AbstractInput] from a given ID.
     *
     * @param id the [AbstractInput] ID to delete
     *
     * @throws [InputException.NotFoundException] it not found
     * @throws [InputException.WriteException] if something goes wrong
     */
    suspend fun deleteInput(id: Long)

    /**
     * Exports [AbstractInput] from a given ID as `JSON` file.
     *
     * @param id the [AbstractInput] ID to export
     * @param settings additional settings
     *
     * @throws [InputException.NotFoundException] it not found
     * @throws [InputException.WriteException] if something goes wrong
     */
    suspend fun exportInput(
        id: Long,
        settings: S? = null
    ): I

    /**
     * Exports [AbstractInput] as `JSON` file.
     *
     * @param input the [AbstractInput] to save
     * @param settings additional settings
     *
     * @throws [InputException.WriteException] if something goes wrong
     */
    suspend fun exportInput(
        input: I,
        settings: S? = null
    ): I
}