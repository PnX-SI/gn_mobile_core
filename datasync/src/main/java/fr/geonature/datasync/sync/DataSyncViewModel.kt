package fr.geonature.datasync.sync

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.interactor.BaseResultUseCase
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.sync.usecase.HasLocalDataUseCase
import fr.geonature.datasync.sync.worker.DataSyncWorker
import kotlinx.coroutines.launch
import org.tinylog.Logger
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration

/**
 * Keeps track of data sync operations from GeoNature.
 *
 * @author S. Grimault
 */
@HiltViewModel
class DataSyncViewModel @Inject constructor(
    application: Application,
    dataSyncManager: IDataSyncManager,
    private val hasLocalDataUseCase: HasLocalDataUseCase
) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(getApplication())

    init {
        dataSyncManager.getLastSynchronizedDate()
    }

    val lastSynchronizedDate: LiveData<Pair<IDataSyncManager.SyncState, Date?>> =
        dataSyncManager.lastSynchronizedDate

    /**
     * Observes the current data sync status directly from WorkManager, without any internal
     * mutable state. This avoids edge cases where the LiveData would stop emitting while the
     * worker was still running (e.g. a missed ENQUEUED → RUNNING transition, or a new worker
     * being started after the ViewModel had already cleared its tracked ID).
     */
    val dataSyncStatus: LiveData<DataSyncStatus?> =
        workManager
            .getWorkInfosByTagLiveData(DataSyncWorker.DATA_SYNC_WORKER_TAG)
            .map { workInfoList ->
                // pick the running worker first matching DataSyncWorker
                val workInfo = workInfoList.firstOrNull { it.tags.contains(DataSyncWorker::class.qualifiedName)}

                if (workInfo == null) {
                    _isSyncRunning.postValue(false)
                    return@map null
                }

                if (workInfo.state !in arrayOf(
                        WorkInfo.State.RUNNING,
                        WorkInfo.State.ENQUEUED
                    )
                ) {
                    _isSyncRunning.postValue(false)
                    return@map null
                }

                _isSyncRunning.postValue(true)

                val serverStatus = ServerStatus.entries[workInfo.progress.getInt(
                    DataSyncWorker.KEY_SERVER_STATUS,
                    workInfo.outputData.getInt(
                        DataSyncWorker.KEY_SERVER_STATUS,
                        ServerStatus.OK.ordinal
                    )
                )]

                DataSyncStatus(
                    workInfo.state,
                    workInfo.progress.getString(DataSyncWorker.KEY_SYNC_MESSAGE)
                        ?: workInfo.outputData.getString(DataSyncWorker.KEY_SYNC_MESSAGE),
                    serverStatus
                )
            }

    private val _isSyncRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    val isSyncRunning: LiveData<Boolean> = _isSyncRunning

    fun hasLocalData(): LiveData<Boolean> =
        liveData {
            hasLocalDataUseCase
                .run(BaseResultUseCase.None())
                .fold(
                    onSuccess = {
                        Logger.debug { "has local data: $it" }

                        emit(it)
                    },
                    onFailure = {
                        emit(false)
                    },
                )
        }

    fun startSync(
        dataSyncSettings: DataSyncSettings,
        withAdditionalFields: Boolean = false,
        notificationComponentClassIntent: Class<*>,
        notificationChannelId: String
    ) {
        Logger.info { "starting local data synchronization..." }

        DataSyncWorker.enqueueUniqueWork(
            getApplication(),
            dataSyncSettings,
            withAdditionalFields,
            notificationComponentClassIntent,
            notificationChannelId
        )
    }

    fun configurePeriodicSync(
        appSettings: DataSyncSettings,
        withAdditionalFields: Boolean = false,
        notificationComponentClassIntent: Class<*>,
        notificationChannelId: String
    ) {
        viewModelScope.launch {
            val alreadyRunning = workManager
                .getWorkInfosByTag(DataSyncWorker.DATA_SYNC_WORKER_TAG)
                .await()
                .any { it.state == WorkInfo.State.RUNNING }

            if (alreadyRunning) {
                Logger.info { "a data synchronization worker is still running: abort the periodic synchronization configuration..." }

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
                Logger.info { "no periodic synchronization configured: abort" }

                return@launch
            }

            // all periodic synchronizations are correctly configured
            if (essentialDataSyncPeriodicity != null && dataSyncPeriodicity != null) {
                configurePeriodicSync(
                    appSettings,
                    dataSyncPeriodicity,
                    withAdditionalData = true,
                    withAdditionalFields,
                    notificationComponentClassIntent,
                    notificationChannelId
                )
                configurePeriodicSync(
                    appSettings,
                    essentialDataSyncPeriodicity,
                    withAdditionalData = false,
                    withAdditionalFields,
                    notificationComponentClassIntent,
                    notificationChannelId
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
                        it,
                        withAdditionalData = true,
                        withAdditionalFields = false,
                        notificationComponentClassIntent,
                        notificationChannelId
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
        withAdditionalData: Boolean = true,
        withAdditionalFields: Boolean = false,
        notificationComponentClassIntent: Class<*>,
        notificationChannelId: String
    ) {
        Logger.info { "configure data sync periodic worker (repeat interval: $repeatInterval, with additional data: $withAdditionalData)..." }

        DataSyncWorker.enqueueUniquePeriodicWork(
            getApplication(),
            dataSyncSettings,
            withAdditionalData,
            withAdditionalFields,
            notificationComponentClassIntent,
            notificationChannelId,
            repeatInterval
        )
    }
}
