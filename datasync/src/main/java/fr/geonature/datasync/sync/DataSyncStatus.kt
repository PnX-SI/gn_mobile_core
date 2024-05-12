package fr.geonature.datasync.sync

import android.content.Context
import androidx.work.WorkInfo
import fr.geonature.datasync.R
import fr.geonature.datasync.api.error.BaseApiException

/**
 * Describes a data synchronization status message.
 *
 * @author S. Grimault
 */
data class DataSyncStatus(
    val state: WorkInfo.State,
    val syncMessage: String? = null,
    val serverStatus: ServerStatus = ServerStatus.OK
) {
    companion object {

        /**
         * Gets a [DataSyncStatus] from given [Exception].
         */
        fun fromException(
            throwable: Throwable,
            context: Context,
            errorMessage: String? = null
        ): DataSyncStatus {
            return when (throwable) {
                is BaseApiException.UnauthorizedException -> {
                    DataSyncStatus(
                        state = WorkInfo.State.FAILED,
                        syncMessage = context.getString(R.string.sync_error_server_not_connected),
                        serverStatus = ServerStatus.UNAUTHORIZED
                    )
                }

                is BaseApiException.InternalServerException -> {
                    DataSyncStatus(
                        state = WorkInfo.State.FAILED,
                        syncMessage = context.getString(R.string.sync_error_server_error),
                        serverStatus = ServerStatus.INTERNAL_SERVER_ERROR
                    )
                }

                else -> {
                    DataSyncStatus(
                        state = WorkInfo.State.FAILED,
                        syncMessage = errorMessage
                    )
                }
            }
        }
    }
}

