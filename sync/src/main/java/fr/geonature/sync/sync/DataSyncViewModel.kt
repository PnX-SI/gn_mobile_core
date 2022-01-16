package fr.geonature.sync.sync

import android.app.Application
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.sync.sync.worker.DataSyncWorker
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Keeps track of data sync operations from GeoNature.
 *
 * @author S. Grimault
 */
class DataSyncViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(getApplication())
    private val dataSyncManager = DataSyncManager
        .getInstance(getApplication())
        .also {
            it.getLastSynchronizedDate()
        }

    private var currentSyncWorkerId: UUID? = null
        set(value) {
            field = value
            _isSyncRunning.postValue(field != null)
        }

    val lastSynchronizedDate: LiveData<Pair<DataSyncManager.SyncState, Date?>> =
        dataSyncManager.lastSynchronizedDate

    private val _isSyncRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    val isSyncRunning: LiveData<Boolean> = _isSyncRunning

    fun observeDataSyncStatus(): LiveData<DataSyncStatus?> {
        return map(workManager.getWorkInfosByTagLiveData(DataSyncWorker.DATA_SYNC_WORKER_TAG)) { workInfoList ->
            if (workInfoList == null || workInfoList.isEmpty()) {
                currentSyncWorkerId = null
                return@map null
            }

            val workInfo = workInfoList.firstOrNull { it.id == currentSyncWorkerId }
                ?: workInfoList.firstOrNull { it.state == WorkInfo.State.RUNNING }

            // no work info is running: abort
            if (workInfo == null) {
                currentSyncWorkerId = null
                return@map null
            }

            // this is a new work info: set the current worker
            if (workInfo.id != currentSyncWorkerId) {
                currentSyncWorkerId = workInfo.id
            }

            val serverStatus = ServerStatus.values()[workInfo.progress.getInt(
                DataSyncWorker.KEY_SERVER_STATUS,
                workInfo.outputData.getInt(
                    DataSyncWorker.KEY_SERVER_STATUS,
                    ServerStatus.OK.ordinal
                )
            )]

            // this work info is not scheduled or not running: the current worker is done
            if (workInfo.state !in arrayListOf(
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING
                )
            ) {
                currentSyncWorkerId = null
            }

            DataSyncStatus(
                workInfo.state,
                workInfo.progress.getString(DataSyncWorker.KEY_SYNC_MESSAGE)
                    ?: workInfo.outputData.getString(DataSyncWorker.KEY_SYNC_MESSAGE),
                serverStatus
            )
        }
    }

    fun startSync(dataSyncSettings: DataSyncSettings) {
        Log.i(
            TAG,
            "starting local data synchronization..."
        )

        val dataSyncWorkRequest = OneTimeWorkRequest
            .Builder(DataSyncWorker::class.java)
            .addTag(DataSyncWorker.DATA_SYNC_WORKER_TAG)
            .setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(DataSyncWorker.inputData(dataSyncSettings))
            .build()

        currentSyncWorkerId = dataSyncWorkRequest.id

        workManager.enqueueUniqueWork(
            DataSyncWorker.DATA_SYNC_WORKER,
            ExistingWorkPolicy.REPLACE,
            dataSyncWorkRequest
        )
    }

    fun configurePeriodicSync(appSettings: DataSyncSettings) {
        viewModelScope.launch {
            val alreadyRunning = workManager
                .getWorkInfosByTag(DataSyncWorker.DATA_SYNC_WORKER_TAG)
                .await()
                .any { it.state == WorkInfo.State.RUNNING }

            if (alreadyRunning) {
                Log.i(
                    TAG,
                    "a data synchronization worker is still running: abort the periodic synchronization configuration..."
                )

                return@launch
            }

            NotificationManagerCompat
                .from(getApplication())
                .cancel(DataSyncWorker.SYNC_NOTIFICATION_ID)

            workManager
                .cancelUniqueWork(DataSyncWorker.DATA_SYNC_WORKER_PERIODIC)
                .await()
            workManager
                .cancelUniqueWork(DataSyncWorker.DATA_SYNC_WORKER_PERIODIC_ESSENTIAL)
                .await()

            val essentialDataSyncPeriodicity = appSettings.essentialDataSyncPeriodicity
            val dataSyncPeriodicity = appSettings.dataSyncPeriodicity

            // no periodic synchronization is correctly configured: abort
            if (essentialDataSyncPeriodicity == null && dataSyncPeriodicity == null) {
                Log.i(
                    TAG,
                    "no periodic synchronization configured: abort"
                )

                return@launch
            }

            // all periodic synchronizations are correctly configured
            if (essentialDataSyncPeriodicity != null && dataSyncPeriodicity != null) {
                configurePeriodicSync(
                    appSettings,
                    dataSyncPeriodicity
                )
                configurePeriodicSync(
                    appSettings,
                    essentialDataSyncPeriodicity,
                    withAdditionalData = false
                )

                return@launch
            }

            // at least one periodic synchronization is correctly configured
            arrayOf(
                essentialDataSyncPeriodicity,
                dataSyncPeriodicity
            )
                .firstOrNull { it != null }
                ?.also {
                    configurePeriodicSync(
                        appSettings,
                        it
                    )
                }
        }
    }

    fun cancelTasks() {
        workManager.cancelAllWorkByTag(DataSyncWorker.DATA_SYNC_WORKER_TAG)
        NotificationManagerCompat
            .from(getApplication())
            .cancel(DataSyncWorker.SYNC_NOTIFICATION_ID)
    }

    private fun configurePeriodicSync(
        dataSyncSettings: DataSyncSettings,
        repeatInterval: Duration,
        withAdditionalData: Boolean = true
    ) {
        Log.i(
            TAG,
            "configure data sync periodic worker (repeat interval: $repeatInterval, with additional data: $withAdditionalData)..."
        )

        val request = PeriodicWorkRequestBuilder<DataSyncWorker>(
            repeatInterval.inWholeSeconds,
            TimeUnit.SECONDS
        )
            .addTag(DataSyncWorker.DATA_SYNC_WORKER_TAG)
            .setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInitialDelay(
                (if (withAdditionalData) DEFAULT_MIN_DURATION else (repeatInterval / 2).coerceAtLeast(30.toDuration(DurationUnit.MINUTES))).inWholeSeconds,
                TimeUnit.SECONDS
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                (if (withAdditionalData) 1 else 2).toDuration(DurationUnit.MINUTES).inWholeSeconds,
                TimeUnit.SECONDS
            )
            .setInputData(
                DataSyncWorker.inputData(
                    dataSyncSettings,
                    withAdditionalData
                )
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            if (withAdditionalData) DataSyncWorker.DATA_SYNC_WORKER_PERIODIC else DataSyncWorker.DATA_SYNC_WORKER_PERIODIC_ESSENTIAL,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Default Factory to use for [DataSyncViewModel].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    class Factory(val creator: () -> DataSyncViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }

    companion object {
        private val TAG = DataSyncViewModel::class.java.name

        private val DEFAULT_MIN_DURATION = 15.toDuration(DurationUnit.MINUTES)
    }
}
