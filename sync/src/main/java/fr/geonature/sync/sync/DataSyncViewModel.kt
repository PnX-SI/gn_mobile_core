package fr.geonature.sync.sync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import fr.geonature.sync.settings.AppSettings
import java.util.Date

/**
 * Keeps track of sync operations.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DataSyncViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(getApplication())
    private val dataSyncManager = DataSyncManager.getInstance(getApplication())
        .also {
            it.getLastSynchronizedDate()
        }

    val dataSyncStatus: LiveData<DataSyncStatus?> =
        map(workManager.getWorkInfosByTagLiveData(DataSyncWorker.DATA_SYNC_WORKER_TAG)) { workInfos ->
            val workInfo = workInfos.firstOrNull() ?: return@map null

            val serverStatus = ServerStatus.values()[workInfo.progress.getInt(
                DataSyncWorker.KEY_SERVER_STATUS,
                workInfo.outputData.getInt(
                    DataSyncWorker.KEY_SERVER_STATUS,
                    ServerStatus.OK.ordinal
                )
            )]

            if (arrayOf(
                    ServerStatus.FORBIDDEN,
                    ServerStatus.INTERNAL_SERVER_ERROR
                ).contains(serverStatus)
            ) {
                cancelTasks()
            }

            DataSyncStatus(
                workInfo.state,
                workInfo.progress.getString(DataSyncWorker.KEY_SYNC_MESSAGE)
                    ?: workInfo.outputData.getString(DataSyncWorker.KEY_SYNC_MESSAGE),
                serverStatus
            )
        }
    val lastSynchronizedDate: LiveData<Date?> = dataSyncManager.lastSynchronizedDate

    fun startSync(appSettings: AppSettings) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dataSyncWorkRequest = OneTimeWorkRequest.Builder(DataSyncWorker::class.java)
            .addTag(DataSyncWorker.DATA_SYNC_WORKER_TAG)
            .setInputData(
                Data.Builder()
                    .putInt(
                        DataSyncWorker.INPUT_USERS_MENU_ID,
                        appSettings.usersListId
                    )
                    .putInt(
                        DataSyncWorker.INPUT_TAXREF_LIST_ID,
                        appSettings.taxrefListId
                    )
                    .build()
            )
            .setConstraints(
                constraints
            )
            .build()

        val continuation = workManager.beginUniqueWork(
            DataSyncWorker.DATA_SYNC_WORKER,
            ExistingWorkPolicy.KEEP,
            dataSyncWorkRequest
        )

        // start the work
        continuation.enqueue()
    }

    private fun cancelTasks() {
        workManager.cancelAllWorkByTag(DataSyncWorker.DATA_SYNC_WORKER_TAG)
    }

    /**
     * Default Factory to use for [DataSyncViewModel].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    class Factory(val creator: () -> DataSyncViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }
}
