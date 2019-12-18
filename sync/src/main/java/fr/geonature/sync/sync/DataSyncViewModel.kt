package fr.geonature.sync.sync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
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

    val syncOutputStatus: LiveData<List<WorkInfo>> = workManager.getWorkInfosByTagLiveData(DataSyncWorker.DATA_SYNC_WORKER_TAG)
    val lastSynchronizedDate: LiveData<Date?> = dataSyncManager.lastSynchronizedDate
    val syncMessage: LiveData<String> = dataSyncManager.syncMessage
    val serverStatus: LiveData<ServerStatus> = Transformations.map(dataSyncManager.serverStatus) { serverStatus ->
        if (serverStatus == null) return@map serverStatus

        when (serverStatus) {
            ServerStatus.FORBIDDEN, ServerStatus.INTERNAL_SERVER_ERROR -> cancelTasks()
        }

        serverStatus
    }

    fun startSync() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val continuation = workManager.beginUniqueWork(DataSyncWorker.DATA_SYNC_WORKER,
                                                       ExistingWorkPolicy.KEEP,
                                                       OneTimeWorkRequest.Builder(DataSyncWorker::class.java).addTag(DataSyncWorker.DATA_SYNC_WORKER_TAG).setConstraints(constraints).build())

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