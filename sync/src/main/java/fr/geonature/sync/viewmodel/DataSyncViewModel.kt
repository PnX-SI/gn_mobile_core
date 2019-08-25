package fr.geonature.sync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.geonature.sync.worker.Constants
import fr.geonature.sync.worker.DataSyncWorker


/**
 * Keeps track of sync operations.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DataSyncViewModel(application: Application) : AndroidViewModel(application) {
    private val workManager: WorkManager = WorkManager.getInstance(getApplication())

    internal val syncOutputStatus: LiveData<List<WorkInfo>>
        get() = workManager.getWorkInfosByTagLiveData(Constants.TAG_DATA_SYNC_OUTPUT)

    fun startSync() {
        val continuation = workManager.beginUniqueWork(Constants.DATA_SYNC_WORK_NAME,
                                                       ExistingWorkPolicy.REPLACE,
                                                       OneTimeWorkRequest.from(DataSyncWorker::class.java))

        // start the work
        continuation.enqueue()
    }
}