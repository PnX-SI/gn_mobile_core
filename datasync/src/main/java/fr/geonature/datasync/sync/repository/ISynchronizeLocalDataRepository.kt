package fr.geonature.datasync.sync.repository

import fr.geonature.datasync.sync.DataSyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Synchronize local data.
 *
 * @author S. Grimault
 */
interface ISynchronizeLocalDataRepository {

    suspend operator fun invoke(): Flow<DataSyncStatus>
}