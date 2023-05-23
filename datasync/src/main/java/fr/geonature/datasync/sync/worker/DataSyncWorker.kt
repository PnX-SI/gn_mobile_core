package fr.geonature.datasync.sync.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager.getInstance
import androidx.work.WorkerParameters
import androidx.work.await
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.geonature.datasync.R
import fr.geonature.datasync.packageinfo.worker.CheckInputsToSynchronizeWorker
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.sync.DataSyncStatus
import fr.geonature.datasync.sync.IDataSyncManager
import fr.geonature.datasync.sync.ServerStatus
import fr.geonature.datasync.sync.usecase.DataSyncUseCase
import fr.geonature.datasync.ui.login.LoginActivity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.onEach
import org.tinylog.Logger
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

/**
 * Local data synchronization worker.
 *
 * @author S. Grimault
 */
@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataSyncUseCase: DataSyncUseCase,
    private val dataSyncManager: IDataSyncManager
) : CoroutineWorker(
    appContext,
    workerParams
) {
    private val workManager = getInstance(applicationContext)

    override suspend fun doWork(): Result {
        val startTime = Date()

        val alreadyRunning = workManager
            .getWorkInfosByTag(DATA_SYNC_WORKER_TAG)
            .await()
            .any { it.id != id && it.state == WorkInfo.State.RUNNING }

        if (alreadyRunning) {
            Logger.warn { "already running: abort" }

            return Result.retry()
        }

        setProgress(workData(applicationContext.getString(R.string.sync_start_synchronization)))
        sendNotification(applicationContext.getString(R.string.sync_start_synchronization))

        val result = dataSyncUseCase
            .run(
                DataSyncUseCase.Params(
                    withAdditionalData = inputData.getBoolean(
                        INPUT_WITH_ADDITIONAL_DATA,
                        true
                    ),
                    usersMenuId = inputData.getInt(
                        INPUT_USERS_MENU_ID,
                        0
                    ),
                    taxRefListId = inputData.getInt(
                        INPUT_TAXREF_LIST_ID,
                        0
                    ),
                    codeAreaType = inputData.getString(INPUT_CODE_AREA_TYPE),
                    pageSize = inputData.getInt(
                        INPUT_PAGE_SIZE,
                        DataSyncSettings.Builder.DEFAULT_PAGE_SIZE
                    )
                )
            )
            .onEach {
                sendNotification(
                    it,
                    if (it.serverStatus == ServerStatus.UNAUTHORIZED) LoginActivity::class.java else null
                )

                if (it.state == WorkInfo.State.SUCCEEDED) {
                    setProgress(
                        workData(
                            it.syncMessage,
                            it.serverStatus
                        )
                    )
                }
            }
            .catch {
                Logger.warn { it.message }
            }
            .lastOrNull()
            ?.let {
                if (it.state == WorkInfo.State.SUCCEEDED) Result.success(
                    workData(
                        it.syncMessage,
                        it.serverStatus
                    )
                ) else Result.failure(
                    workData(
                        it.syncMessage,
                        it.serverStatus
                    )
                )
            }?: Result.failure()

        Logger.info { "local data synchronization ${if (result is Result.Success) "successfully finished" else "finished with failed tasks"} in ${Date().time - startTime.time}ms" }

        if (result is Result.Success) {
            NotificationManagerCompat
                .from(applicationContext)
                .cancel(SYNC_NOTIFICATION_ID)

            dataSyncManager.updateLastSynchronizedDate(
                inputData.getBoolean(
                    INPUT_WITH_ADDITIONAL_DATA,
                    true
                )
            )

            return Result.success(workData(applicationContext.getString(R.string.sync_data_succeeded)))
        }

        return result
    }

    private suspend fun sendNotification(
        dataSyncStatus: DataSyncStatus,
        componentClassIntent: Class<*>? = null
    ) {
        sendNotification(
            if (dataSyncStatus.serverStatus == ServerStatus.UNAUTHORIZED) applicationContext.getString(R.string.sync_error_server_not_connected)
            else dataSyncStatus.syncMessage,
            componentClassIntent
        )
    }

    private suspend fun sendNotification(
        contentText: CharSequence?,
        componentClassIntent: Class<*>? = null,
        notificationId: Int = SYNC_NOTIFICATION_ID
    ) {
        val notificationChannelId = inputData.getString(INPUT_NOTIFICATION_CHANNEL_ID)
        val intentClassName = inputData.getString(INPUT_INTENT_CLASS_NAME)

        if (notificationChannelId.isNullOrBlank() || intentClassName.isNullOrBlank()) {
            return
        }

        val componentClassIntentOrDefault =
            runCatching { Class.forName(intentClassName) }.getOrElse { componentClassIntent }

        if (componentClassIntentOrDefault == null) {
            Logger.warn { "no notification will be sent as intent class name '$intentClassName' was not found" }

            return
        }

        setForeground(
            ForegroundInfo(
                notificationId,
                NotificationCompat
                    .Builder(
                        applicationContext,
                        notificationChannelId
                    )
                    .setAutoCancel(true)
                    .setContentTitle(applicationContext.getText(R.string.notification_data_synchronization_title))
                    .setContentText(contentText)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            applicationContext,
                            0,
                            Intent(
                                applicationContext,
                                componentClassIntentOrDefault
                            ).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            },
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                        )
                    )
                    .setSmallIcon(R.drawable.ic_sync)
                    .build()
            )
        )
    }

    private fun workData(
        syncMessage: String?,
        serverStatus: ServerStatus = ServerStatus.OK
    ): Data {
        return workDataOf(
            KEY_SYNC_MESSAGE to syncMessage,
            KEY_SERVER_STATUS to serverStatus.ordinal
        )
    }

    companion object {

        const val KEY_SYNC_MESSAGE = "KEY_SYNC_MESSAGE"
        const val KEY_SERVER_STATUS = "KEY_SERVER_STATUS"

        // the name of the synchronization work
        private const val DATA_SYNC_WORKER = "data_sync_worker"
        const val DATA_SYNC_WORKER_PERIODIC = "data_sync_worker_periodic"
        const val DATA_SYNC_WORKER_PERIODIC_ESSENTIAL = "data_sync_worker_periodic_essential"
        const val DATA_SYNC_WORKER_TAG = "data_sync_worker_tag"

        const val SYNC_NOTIFICATION_ID = 3
        const val AUTH_NOTIFICATION_ID = 4

        private const val INPUT_USERS_MENU_ID = "usersMenuId"
        private const val INPUT_TAXREF_LIST_ID = "taxrefListId"
        private const val INPUT_CODE_AREA_TYPE = "codeAreaType"
        private const val INPUT_PAGE_SIZE = "pageSize"
        private const val INPUT_WITH_ADDITIONAL_DATA = "withAdditionalData"
        private const val INPUT_INTENT_CLASS_NAME = "intent_class_name"
        private const val INPUT_NOTIFICATION_CHANNEL_ID = "notification_channel_id"
        const val DEFAULT_CHANNEL_DATA_SYNCHRONIZATION = "channel_data_synchronization"

        /**
         * Convenience method for enqueuing unique work to this worker.
         */
        fun enqueueUniqueWork(
            context: Context,
            dataSyncSettings: DataSyncSettings,
            notificationComponentClassIntent: Class<*>,
            notificationChannelId: String
        ): UUID {
            val dataSyncWorkRequest = OneTimeWorkRequest
                .Builder(DataSyncWorker::class.java)
                .addTag(DATA_SYNC_WORKER_TAG)
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    inputData(
                        dataSyncSettings,
                        true,
                        notificationComponentClassIntent,
                        notificationChannelId
                    )
                )
                .build()

            return dataSyncWorkRequest.id.also {
                getInstance(context).enqueueUniqueWork(
                    DATA_SYNC_WORKER,
                    ExistingWorkPolicy.KEEP,
                    dataSyncWorkRequest
                )
            }
        }

        /**
         * Convenience method for enqueuing periodic work to this worker.
         */
        fun enqueueUniquePeriodicWork(
            context: Context,
            dataSyncSettings: DataSyncSettings,
            withAdditionalData: Boolean = true,
            notificationComponentClassIntent: Class<*>,
            notificationChannelId: String = DEFAULT_CHANNEL_DATA_SYNCHRONIZATION,
            repeatInterval: Duration = 15.toDuration(DurationUnit.MINUTES)
        ) {
            getInstance(context).enqueueUniquePeriodicWork(
                if (withAdditionalData) DATA_SYNC_WORKER_PERIODIC else DATA_SYNC_WORKER_PERIODIC_ESSENTIAL,
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<CheckInputsToSynchronizeWorker>(repeatInterval.toJavaDuration())
                    .addTag(DATA_SYNC_WORKER_TAG)
                    .setConstraints(
                        Constraints
                            .Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .setInitialDelay(
                        (if (withAdditionalData) 1.toDuration(DurationUnit.MINUTES) else 15.toDuration(DurationUnit.MINUTES)).inWholeSeconds,
                        TimeUnit.SECONDS
                    )
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        (if (withAdditionalData) 1 else 2).toDuration(DurationUnit.MINUTES).inWholeSeconds,
                        TimeUnit.SECONDS
                    )
                    .setInputData(
                        inputData(
                            dataSyncSettings,
                            withAdditionalData,
                            notificationComponentClassIntent,
                            notificationChannelId
                        )
                    )
                    .build()
            )
        }

        /**
         * Configure input data to this worker from given [DataSyncSettings].
         */
        private fun inputData(
            dataSyncSettings: DataSyncSettings,
            withAdditionalData: Boolean = true,
            notificationComponentClassIntent: Class<*>,
            notificationChannelId: String
        ): Data {
            return Data
                .Builder()
                .putInt(
                    INPUT_USERS_MENU_ID,
                    dataSyncSettings.usersListId
                )
                .putInt(
                    INPUT_TAXREF_LIST_ID,
                    dataSyncSettings.taxrefListId
                )
                .putString(
                    INPUT_CODE_AREA_TYPE,
                    dataSyncSettings.codeAreaType
                )
                .putInt(
                    INPUT_PAGE_SIZE,
                    dataSyncSettings.pageSize
                )
                .putBoolean(
                    INPUT_WITH_ADDITIONAL_DATA,
                    withAdditionalData
                )
                .putString(
                    INPUT_INTENT_CLASS_NAME,
                    notificationComponentClassIntent.name
                )
                .putString(
                    INPUT_NOTIFICATION_CHANNEL_ID,
                    notificationChannelId
                )
                .build()
        }
    }
}
