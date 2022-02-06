package fr.geonature.datasync.sync

import androidx.work.WorkInfo

/**
 * Describes a data synchronization status message.
 *
 * @author S. Grimault
 */
data class DataSyncStatus(
    val state: WorkInfo.State,
    val syncMessage: String?,
    val serverStatus: ServerStatus = ServerStatus.OK
)
