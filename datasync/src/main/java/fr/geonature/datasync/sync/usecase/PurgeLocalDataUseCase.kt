package fr.geonature.datasync.sync.usecase

import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.dao.AppSyncDao
import fr.geonature.commons.interactor.BaseResultUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Purge all existing local datas.
 *
 * @author S. Grimault
 */
class PurgeLocalDataUseCase @Inject constructor(
    private val database: LocalDatabase,
    private val appSyncDao: AppSyncDao
) : BaseResultUseCase<Boolean, PurgeLocalDataUseCase.Params>() {
    override suspend fun run(params: Params): Result<Boolean> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (params.purgeDatabase) {
                    database.clearAllTables()
                    database
                        .taxonDao()
                        .deleteAll()
                }
                appSyncDao.clearLastSynchronizedDate()
                true
            }
        }

    data class Params(
        val purgeDatabase: Boolean = true
    )
}