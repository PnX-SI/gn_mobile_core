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
import java.util.UUID
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

    private var currentSyncWorkerId: UUID? = null
        set(value) {
            field = value
            _isSyncRunning.postValue(field != null)
        }

    val lastSynchronizedDate: LiveData<Pair<IDataSyncManager.SyncState, Date?>> =
        dataSyncManager.lastSynchronizedDate

    private val _isSyncRunning: MutableLiveData<Boolean> = MutableLiveData(false)
    val isSyncRunning: LiveData<Boolean> = _isSyncRunning

    fun hasLocalData(): LiveData<Boolean> =
        liveData {
            hasLocalDataUseCase
                .run(BaseResultUseCase.None())
                .fold(
                    onSuccess = {
                        Logger.debug { "has local data: $it" }

                        emit(true)
                    },
                    onFailure = {
                        emit(false)
                    },
                )
        }

    fun observeDataSyncStatus(): LiveData<DataSyncStatus?> {
        return workManager
            .getWorkInfosByTagLiveData(DataSyncWorker.DATA_SYNC_WORKER_TAG)
            .map { workInfoList ->
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

    fun startSync(
        dataSyncSettings: DataSyncSettings,
        withAdditionalFields: Boolean = false,
        notificationComponentClassIntent: Class<*>,
        notificationChannelId: String
    ) {
        Logger.info { "starting local data synchronization..." }

        currentSyncWorkerId = DataSyncWorker.enqueueUniqueWork(
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
