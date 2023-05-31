package fr.geonature.datasync.sync.repository

import fr.geonature.datasync.sync.DataSyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Synchronize additional data.
 *
 * @author S. Grimault
 */
interface ISynchronizeAdditionalDataRepository {

    suspend operator fun invoke(): Flow<DataSyncStatus>
}