package fr.geonature.commons.features.inputObservers.data

import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.features.inputObservers.error.InputObserverException

/**
 * [InputObserver] local data source.
 *
 * @author S. Grimault
 */
interface IInputObserverLocalDataSource {

    /**
     * Finds all [InputObserver]s matching given IDs.
     *
     * @param inputObserverId the [InputObserver] identifier to find
     *
     * @return a list of [InputObserver]s found from given IDs
     * @throws InputObserverException.NoInputObserversFoundException if none was found
     */
    suspend fun findInputObserversByIds(vararg inputObserverId: Long): List<InputObserver>
}