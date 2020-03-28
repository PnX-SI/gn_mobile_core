package fr.geonature.sync.sync

import androidx.work.WorkInfo

/**
 * Describes a data synchronization status message.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class DataSyncStatus(
    val state: WorkInfo.State,
    val syncMessage: String?,
    val serverStatus: ServerStatus = ServerStatus.OK
)