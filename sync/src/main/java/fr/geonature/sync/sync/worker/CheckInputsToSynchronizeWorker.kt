package fr.geonature.sync.sync.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.geonature.sync.MainApplication
import fr.geonature.sync.R
import fr.geonature.sync.sync.PackageInfoManager
import fr.geonature.sync.ui.home.HomeActivity

/**
 * Checks Inputs to synchronize.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class CheckInputsToSynchronizeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {
    private val packageInfoManager =
        PackageInfoManager.getInstance(applicationContext)

    override suspend fun doWork(): Result {
        val availablePackageInfos = packageInfoManager.getInstalledApplications()
        val availableInputs =
            availablePackageInfos.map { packageInfo -> packageInfoManager.getInputsToSynchronize(packageInfo) }
                .flatten()

        val inputsToSynchronize = availableInputs.size

        Log.i(
            TAG,
            "available inputs to synchronize: $inputsToSynchronize"
        )

        with(NotificationManagerCompat.from(applicationContext)) {
            cancel(SYNC_NOTIFICATION_ID)

            if (inputsToSynchronize > 0) {
                notify(
                    SYNC_NOTIFICATION_ID,
                    NotificationCompat.Builder(
                            applicationContext,
                            MainApplication.SYNC_CHANNEL_ID
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
                                    HomeActivity::class.java
                                ).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                },
                                0
                            )
                        )
                        .setSmallIcon(R.drawable.ic_sync)
                        .setNumber(inputsToSynchronize)
                        .build()
                )
            }
        }

        return Result.success()
    }

    companion object {
        private val TAG = CheckInputsToSynchronizeWorker::class.java.name

        const val CHECK_INPUTS_TO_SYNC_WORKER = "check_inputs_to_sync_worker"
        const val CHECK_INPUTS_TO_SYNC_WORKER_TAG = "check_inputs_to_sync_worker_tag"
        const val SYNC_NOTIFICATION_ID = 2
    }
}