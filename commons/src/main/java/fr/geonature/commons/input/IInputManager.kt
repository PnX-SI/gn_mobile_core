package fr.geonature.commons.input

import androidx.lifecycle.LiveData
import fr.geonature.commons.settings.IAppSettings

/**
 * Manage [AbstractInput]:
 * - Create a new [AbstractInput]
 * - Read the current [AbstractInput]
 * - Save the current [AbstractInput]
 * - Export the current [AbstractInput] as `JSON` file
 *
 * @author S. Grimault
 */
interface IInputManager<I : AbstractInput, S: IAppSettings> {

    /***
     * All loaded [AbstractInput]s as `List`.
     */
    val inputs: LiveData<List<I>>

    /**
     * Current loaded [AbstractInput] to edit.
     */
    val input: LiveData<I?>

    /**
     * Reads all [AbstractInput]s.
     *
     * @return A list of [AbstractInput]s
     */
    suspend fun readInputs(): List<I>

    /**
     * Reads [AbstractInput] from given ID.
     *
     * @param id The [AbstractInput] ID to read. If omitted, read the current saved [AbstractInput].
     *
     * @return [AbstractInput] or `null` if not found
     */
    suspend fun readInput(id: Long? = null): I?

    /**
     * Reads the current [AbstractInput].
     *
     * @return [AbstractInput] or `null` if not found
     */
    suspend fun readCurrentInput(): I?

    /**
     * Saves the given [AbstractInput] and sets it as default current [AbstractInput].
     *
     * @param input the [AbstractInput] to save
     *
     * @return `true` if the given [AbstractInput] has been successfully saved, `false` otherwise
     */
    suspend fun saveInput(input: I): Boolean

    /**
     * Deletes [AbstractInput] from given ID.
     *
     * @param id the [AbstractInput] ID to delete
     *
     * @return `true` if the given [AbstractInput] has been successfully deleted, `false` otherwise
     */
    suspend fun deleteInput(id: Long): Boolean

    /**
     * Exports [AbstractInput] from given ID as `JSON` file.
     *
     * @param id the [AbstractInput] ID to export
     * @param settings additional settings
     *
     * @return `true` if the given [AbstractInput] has been successfully exported, `false` otherwise
     */
    suspend fun exportInput(
        id: Long,
        settings: S? = null
    ): Boolean

    /**
     * Exports [AbstractInput] as `JSON` file.
     *
     * @param input the [AbstractInput] to save
     * @param settings additional settings
     *
     * @return `true` if the given [AbstractInput] has been successfully exported, `false` otherwise
     */
    suspend fun exportInput(
        input: I,
        settings: S? = null
    ): Boolean
}