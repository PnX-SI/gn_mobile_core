package fr.geonature.sync.sync.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.geonature.sync.MainApplication
import fr.geonature.sync.R
import fr.geonature.sync.ui.login.LoginActivity

/**
 * Checks if the current user session is still valid.
 *
 * @author S. Grimault
 */
class CheckAuthLoginWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {
    override suspend fun doWork(): Result {
        val authManager = (applicationContext as MainApplication).sl.authManager

        // not connected: notify user
        if (authManager.getAuthLogin() == null) {
            with(NotificationManagerCompat.from(applicationContext)) {
                cancel(NOTIFICATION_ID)
                notify(
                    NOTIFICATION_ID,
                    NotificationCompat
                        .Builder(
                            applicationContext,
                            MainApplication.CHANNEL_DATA_SYNCHRONIZATION
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
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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
        const val CHECK_AUTH_LOGIN_WORKER = "check_auth_login_worker"
        const val CHECK_AUTH_LOGIN_WORKER_TAG = "check_auth_login_worker_tag"
        const val NOTIFICATION_ID = 4
    }
}