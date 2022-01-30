package fr.geonature.datasync.auth.worker

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
import fr.geonature.datasync.auth.IAuthManager
import fr.geonature.datasync.ui.login.LoginActivity
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

/**
 * Checks if the current user session is still valid.
 *
 * @author S. Grimault
 */
@HiltWorker
class CheckAuthLoginWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authManager: IAuthManager
) : CoroutineWorker(
    appContext,
    workerParams
) {
    override suspend fun doWork(): Result {
        // not connected: notify user
        if (authManager.getAuthLogin() == null) {
            with(NotificationManagerCompat.from(applicationContext)) {
                cancel(NOTIFICATION_ID)
                notify(
                    NOTIFICATION_ID,
                    NotificationCompat
                        .Builder(
                            applicationContext,
                            inputData.getString(KEY_NOTIFICATION_CHANNEL_ID)
                                ?: DEFAULT_CHANNEL_DATA_SYNCHRONIZATION
                        )
                        .setAutoCancel(true)
                        .setContentTitle(applicationContext.getText(R.string.notification_data_synchronization_title))
                        .setContentText(applicationContext.getString(R.string.sync_error_server_not_connected))
                        .setContentIntent(
                            PendingIntent.getActivity(
                                applicationContext,
                                0,
                                Intent(
                                    applicationContext,
                                    LoginActivity::class.java
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
            }
        }

        return Result.success()
    }

    companion object {
        private const val CHECK_AUTH_LOGIN_WORKER = "check_auth_login_worker"
        private const val CHECK_AUTH_LOGIN_WORKER_TAG = "check_auth_login_worker_tag"
        const val NOTIFICATION_ID = 4

        private const val KEY_NOTIFICATION_CHANNEL_ID = "notification_channel_id"
        const val DEFAULT_CHANNEL_DATA_SYNCHRONIZATION = "channel_data_synchronization"

        /**
         * Convenience method for enqueuing periodic work to this worker.
         */
        fun enqueueUniquePeriodicWork(
            context: Context,
            notificationChannelId: String = DEFAULT_CHANNEL_DATA_SYNCHRONIZATION,
            duration: Duration = 1.toDuration(DurationUnit.HOURS)
        ) {
            getInstance(context).enqueueUniquePeriodicWork(
                CHECK_AUTH_LOGIN_WORKER,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<CheckAuthLoginWorker>(duration.toJavaDuration())
                    .addTag(CHECK_AUTH_LOGIN_WORKER_TAG)
                    .setInputData(
                        Data
                            .Builder()
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