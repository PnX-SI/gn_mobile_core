package fr.geonature.sync.sync

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import fr.geonature.commons.util.add
import fr.geonature.commons.util.toIsoDateString
import fr.geonature.sync.settings.AppSettings
import fr.geonature.sync.sync.worker.DataSyncWorker
import java.util.Calendar
import java.util.Date

/**
 * Keeps track of data sync operations from GeoNature.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DataSyncViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(getApplication())
    private val dataSyncManager = DataSyncManager.getInstance(getApplication())
        .also {
            it.getLastSynchronizedDate()
        }

    val lastSynchronizedDate: LiveData<Date?> = dataSyncManager.lastSynchronizedDate

    fun startSync(appSettings: AppSettings): LiveData<DataSyncStatus?> {
        val lastSynchronizedDate = dataSyncManager.lastSynchronizedDate.value

        if (lastSynchronizedDate?.add(
                Calendar.HOUR,
                1
            )
                ?.after(Date()) == true
        ) {
            Log.d(
                TAG,
                "data already synchronized at ${lastSynchronizedDate.toIsoDateString()}"
            )

            return MutableLiveData(null)
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val dataSyncWorkRequest = OneTimeWorkRequest.Builder(DataSyncWorker::class.java)
            .addTag(DataSyncWorker.DATA_SYNC_WORKER_TAG)
            .setConstraints(constraints)
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
                    .putString(
                        DataSyncWorker.INPUT_CODE_AREA_TYPE,
                        appSettings.codeAreaType
                    )
                    .putInt(
                        DataSyncWorker.INPUT_PAGE_SIZE,
                        appSettings.pageSize
                    )
                    .putInt(
                        DataSyncWorker.INPUT_PAGE_MAX_RETRY,
                        appSettings.pageMaxRetry
                    )
                    .build()
            )
            .build()

        val continuation = workManager.beginUniqueWork(
            DataSyncWorker.DATA_SYNC_WORKER,
            ExistingWorkPolicy.REPLACE,
            dataSyncWorkRequest
        )

        return map(workManager.getWorkInfoByIdLiveData(dataSyncWorkRequest.id)) {
            if (it == null) {
                return@map null
            }

            val serverStatus = ServerStatus.values()[it.progress.getInt(
                DataSyncWorker.KEY_SERVER_STATUS,
                it.outputData.getInt(
                    DataSyncWorker.KEY_SERVER_STATUS,
                    ServerStatus.OK.ordinal
                )
            )]

            DataSyncStatus(
                it.state,
                it.progress.getString(DataSyncWorker.KEY_SYNC_MESSAGE)
                    ?: it.outputData.getString(DataSyncWorker.KEY_SYNC_MESSAGE),
                serverStatus
            )
        }.also {
            // start the work
            continuation.enqueue()
        }
    }

    fun cancelTasks() {
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

    companion object {
        private val TAG = DataSyncViewModel::class.java.name
    }
}
