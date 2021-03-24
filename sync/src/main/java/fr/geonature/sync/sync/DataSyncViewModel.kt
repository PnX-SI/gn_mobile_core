package fr.geonature.sync.sync

import android.app.Application
import android.util.Log
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
import fr.geonature.sync.settings.AppSettings
import fr.geonature.sync.sync.worker.DataSyncWorker
import fr.geonature.sync.util.parseAsDuration
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

/**
 * Keeps track of data sync operations from GeoNature.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
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

    val lastSynchronizedDate: LiveData<Date?> = dataSyncManager.lastSynchronizedDate

    private val _isSyncRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    val isSyncRunning: LiveData<Boolean> = _isSyncRunning

    fun observeDataSyncStatus(): LiveData<DataSyncStatus?> {
        return map(workManager.getWorkInfosByTagLiveData(DataSyncWorker.DATA_SYNC_WORKER_TAG)) { workInfos ->
            if (workInfos == null || workInfos.isEmpty()) {
                currentSyncWorkerId = null
                return@map null
            }

            val workInfo = (if (currentSyncWorkerId == null) workInfos[0] else workInfos.firstOrNull { it.id == currentSyncWorkerId })
                ?: workInfos[0]

            // this work info is not scheduled or not running and no current worker is running: abort
            if (currentSyncWorkerId == null && workInfo.state !in arrayListOf(
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING
                )
            ) {
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

    fun startSync(appSettings: AppSettings) {
        val dataSyncWorkRequest = OneTimeWorkRequest
            .Builder(DataSyncWorker::class.java)
            .addTag(DataSyncWorker.DATA_SYNC_WORKER_TAG)
            .setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(DataSyncWorker.inputData(appSettings))
            .build()

        currentSyncWorkerId = dataSyncWorkRequest.id

        workManager.enqueueUniqueWork(
            DataSyncWorker.DATA_SYNC_WORKER,
            ExistingWorkPolicy.REPLACE,
            dataSyncWorkRequest
        )
    }

    @ExperimentalTime
    fun configurePeriodicSync(appSettings: AppSettings) {
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

            val essentialDataSyncPeriodicity = appSettings.essentialDataSyncPeriodicity?.parseAsDuration()
                ?: Duration.ZERO
            val dataSyncPeriodicity = appSettings.dataSyncPeriodicity?.parseAsDuration()
                ?: Duration.ZERO

            // no periodic synchronization is correctly configured: abort
            if ((arrayOf(
                    essentialDataSyncPeriodicity,
                    dataSyncPeriodicity
                ).all { it < 15.toDuration(DurationUnit.MINUTES) })
            ) {
                Log.w(
                    TAG,
                    "no periodic synchronization is correctly configured: abort"
                )

                return@launch
            }

            // all periodic synchronizations are correctly configured
            if ((arrayOf(
                    essentialDataSyncPeriodicity,
                    dataSyncPeriodicity
                ).all { it >= 15.toDuration(DurationUnit.MINUTES) })
            ) {
                if (essentialDataSyncPeriodicity >= dataSyncPeriodicity) {
                    configurePeriodicSync(
                        appSettings,
                        dataSyncPeriodicity
                    )

                    return@launch
                }

                configurePeriodicSync(
                    appSettings,
                    essentialDataSyncPeriodicity,
                    withAdditionalData = false
                )
                configurePeriodicSync(
                    appSettings,
                    dataSyncPeriodicity
                )

                return@launch
            }

            // at least one periodic synchronization is correctly configured
            arrayOf(
                essentialDataSyncPeriodicity,
                dataSyncPeriodicity
            )
                .firstOrNull { it >= 15.toDuration(DurationUnit.MINUTES) }
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
    }

    @ExperimentalTime
    private fun configurePeriodicSync(
        appSettings: AppSettings,
        repeatInterval: Duration,
        withAdditionalData: Boolean = true
    ) {
        if (repeatInterval.inMinutes < 15) {
            return
        }

        Log.i(
            TAG,
            "configure data sync periodic worker (repeat interval: $repeatInterval, with additional data: $withAdditionalData)..."
        )

        val request = PeriodicWorkRequestBuilder<DataSyncWorker>(
            repeatInterval.inSeconds.toLong(),
            TimeUnit.SECONDS
        )
            .addTag(DataSyncWorker.DATA_SYNC_WORKER_TAG)
            .setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                (repeatInterval.inSeconds.toLong() * 1.25).toLong(),
                TimeUnit.SECONDS
            )
            .setInputData(
                DataSyncWorker.inputData(
                    appSettings,
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
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }

    companion object {
        private val TAG = DataSyncViewModel::class.java.name
    }
}
