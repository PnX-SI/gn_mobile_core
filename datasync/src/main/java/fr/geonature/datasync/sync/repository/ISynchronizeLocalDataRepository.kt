package fr.geonature.datasync.sync.repository

import androidx.work.WorkInfo
import fr.geonature.datasync.sync.DataSyncStatus
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * Synchronize local data.
 *
 * @author S. Grimault
 */
interface ISynchronizeLocalDataRepository<in Params> {

    suspend operator fun invoke(params: Params): Flow<DataSyncStatus>

    companion object {

        /**
         * Sends synchronization status to the given channel and throw exception if its current status
         * is [WorkInfo.State.FAILED] to cancel the current transaction.
         */
        suspend fun sendOrThrow(
            channel: SendChannel<DataSyncStatus>,
            dataSyncStatus: DataSyncStatus
        ) {
            channel.send(dataSyncStatus)

            if (dataSyncStatus.state == WorkInfo.State.FAILED) {
                delay(500)
                throw java.lang.Exception(dataSyncStatus.syncMessage)
            }
        }
    }
}