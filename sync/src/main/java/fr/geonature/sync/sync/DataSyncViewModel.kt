package fr.geonature.sync.sync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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

    val syncOutputStatus: LiveData<List<WorkInfo>> = workManager.getWorkInfosByTagLiveData(DataSyncWorker.TAG_DATA_SYNC_OUTPUT)
    val lastSynchronizedDate: LiveData<Date?> = dataSyncManager.lastSynchronizedDate
    val syncMessage: LiveData<String> = dataSyncManager.syncMessage

    fun startSync() {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val continuation = workManager.beginUniqueWork(DataSyncWorker.DATA_SYNC_WORK_NAME,
                                                       ExistingWorkPolicy.REPLACE,
                                                       OneTimeWorkRequest.Builder(DataSyncWorker::class.java)
                                                               .addTag(DataSyncWorker.TAG_DATA_SYNC_OUTPUT)
                                                               .setConstraints(constraints)
                                                               .build())

        // start the work
        continuation.enqueue()
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