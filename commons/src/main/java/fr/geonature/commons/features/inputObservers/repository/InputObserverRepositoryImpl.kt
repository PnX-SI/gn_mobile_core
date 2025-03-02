package fr.geonature.commons.features.inputObservers.repository

import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.features.inputObservers.data.IInputObserverLocalDataSource

/**
 * Default implementation of [IInputObserverRepository].
 *
 * @author S. Grimault
 */
class InputObserverRepositoryImpl(private val inputObserverLocalDataSource: IInputObserverLocalDataSource) :
    IInputObserverRepository {

    override suspend fun findInputObserversByIds(vararg inputObserverId: Long): Result<List<InputObserver>> {
        return runCatching {
            inputObserverLocalDataSource.findInputObserversByIds(*inputObserverId)
        }
    }
}