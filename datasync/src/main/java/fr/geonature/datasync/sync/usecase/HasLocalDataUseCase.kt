package fr.geonature.datasync.sync.usecase

import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.interactor.BaseResultUseCase
import javax.inject.Inject

/**
 * Whether we have some local data from _GeoNature_ APIs.
 *
 * @author S. Grimault
 */
class HasLocalDataUseCase @Inject constructor(private val database: LocalDatabase) :
    BaseResultUseCase<Boolean, BaseResultUseCase.None>() {

    override suspend fun run(params: None): Result<Boolean> {
        return runCatching {
            !database
                .inputObserverDao()
                .isEmpty() && !database
                .taxonomyDao()
                .isEmpty() && !database
                .datasetDao()
                .isEmpty() && !database
                .nomenclatureTypeDao()
                .isEmpty() && !database
                .taxonDao()
                .isEmpty()
        }
    }
}