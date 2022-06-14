package fr.geonature.datasync.packageinfo.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager.getInstance
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.geonature.datasync.R
import fr.geonature.datasync.packageinfo.IPackageInfoRepository
import fr.geonature.datasync.sync.worker.DataSyncWorker
import org.tinylog.Logger
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

/**
 * Checks Inputs to synchronize.
 *
 * @author S. Grimault
 */
@HiltWorker
class CheckInputsToSynchronizeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val packageInfoRepository: IPackageInfoRepository
) : CoroutineWorker(
    appContext,
    workerParams
) {
    override suspend fun doWork(): Result {
        val availableInputs = packageInfoRepository
            .getInstalledApplications()
            .map { packageInfo -> packageInfo.getInputsToSynchronize(applicationContext) }
            .flatten()

        val inputsToSynchronize = availableInputs.size

        Logger.info { "available inputs to synchronize: $inputsToSynchronize" }

        with(NotificationManagerCompat.from(applicationContext)) {
            cancel(SYNC_NOTIFICATION_ID)

            inputData
                .getString(KEY_INTENT_CLASS_NAME)
                ?.also {
                    if (inputsToSynchronize > 0) {
                        notify(
                            SYNC_NOTIFICATION_ID,
                            NotificationCompat
                                .Builder(
                                    applicationContext,
                                    inputData.getString(KEY_NOTIFICATION_CHANNEL_ID)
                                        ?: DataSyncWorker.DEFAULT_CHANNEL_DATA_SYNCHRONIZATION
                                )
                                .setContentTitle(applicationContext.getText(R.string.notification_inputs_to_synchronize_title))
                                .setContentText(
                                    applicationContext.resources.getQuantityString(
                                        R.plurals.notification_inputs_to_synchronize_description,
                                        inputsToSynchronize,
                                        inputsToSynchronize
                                    )
                                )
                                .setContentIntent(
                                    PendingIntent.getActivity(
                                        applicationContext,
                                        0,
                                        Intent(
                                            applicationContext,
                                            Class.forName(it)
                                        ).apply {
                                            flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        },
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                                    )
                                )
                                .setSmallIcon(R.drawable.ic_sync)
                                .setNumber(inputsToSynchronize)
                                .build()
                        )
                    }
                }
        }

        return Result.success()
    }

    companion object {

        private const val CHECK_INPUTS_TO_SYNC_WORKER = "check_inputs_to_sync_worker"
        private const val CHECK_INPUTS_TO_SYNC_WORKER_TAG = "check_inputs_to_sync_worker_tag"
        const val SYNC_NOTIFICATION_ID = 2

        private const val KEY_INTENT_CLASS_NAME = "intent_class_name"
        private const val KEY_NOTIFICATION_CHANNEL_ID = "notification_channel_id"

        /**
         * Convenience method for enqueuing periodic work to this worker.
         */
        fun enqueueUniquePeriodicWork(
            context: Context,
            notificationComponentClassIntent: Class<*>,
            notificationChannelId: String = DataSyncWorker.DEFAULT_CHANNEL_DATA_SYNCHRONIZATION,
            repeatInterval: Duration = 15.toDuration(DurationUnit.MINUTES)
        ) {
            getInstance(context).enqueueUniquePeriodicWork(
                CHECK_INPUTS_TO_SYNC_WORKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<CheckInputsToSynchronizeWorker>(repeatInterval.toJavaDuration())
                    .addTag(CHECK_INPUTS_TO_SYNC_WORKER_TAG)
                    .setInputData(
                        Data
                            .Builder()
                            .putString(
                                KEY_INTENT_CLASS_NAME,
                                notificationComponentClassIntent.name
                            )
                            .putString(
                                KEY_NOTIFICATION_CHANNEL_ID,
                                notificationChannelId
                            )
                            .build()
                    )
                    .build()
            )
        }
    }
}
