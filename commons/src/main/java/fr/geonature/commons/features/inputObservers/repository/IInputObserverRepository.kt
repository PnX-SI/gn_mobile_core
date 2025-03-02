package fr.geonature.commons.features.inputObservers.repository

import fr.geonature.commons.data.entity.InputObserver

/**
 * [InputObserver] repository.
 *
 * @author S. Grimault
 */
interface IInputObserverRepository {

    /**
     * Finds all [InputObserver]s matching given IDs.
     *
     * @param inputObserverId the [InputObserver] identifier to find
     *
     * @return a list of [InputObserver]s found from given IDs or [Result.Failure] if something goes wrong
     */
    suspend fun findInputObserversByIds(vararg inputObserverId: Long): Result<List<InputObserver>>
}