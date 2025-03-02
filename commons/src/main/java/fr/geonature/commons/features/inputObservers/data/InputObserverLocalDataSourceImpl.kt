package fr.geonature.commons.features.inputObservers.data

import fr.geonature.commons.data.dao.InputObserverDao
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.features.inputObservers.error.InputObserverException

/**
 * Default implementation of [IInputObserverLocalDataSource] using local database.
 *
 * @author S. Grimault
 */
class InputObserverLocalDataSourceImpl(private val inputObserverDao: InputObserverDao) :
    IInputObserverLocalDataSource {

    override suspend fun findInputObserversByIds(vararg inputObserverId: Long): List<InputObserver> {
        return inputObserverDao
            .findByIds(*inputObserverId)
            .takeIf { it.isNotEmpty() }
            ?: throw InputObserverException.NoInputObserversFoundException(inputObserverId.toList())
    }
}