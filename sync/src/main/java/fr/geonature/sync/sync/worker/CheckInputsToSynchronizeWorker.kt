package fr.geonature.sync.sync.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.geonature.sync.MainApplication
import fr.geonature.sync.R
import fr.geonature.sync.sync.IPackageInfoManager
import fr.geonature.sync.ui.home.HomeActivity
import kotlinx.coroutines.flow.firstOrNull

/**
 * Checks Inputs to synchronize.
 *
 * @author S. Grimault
 */
@HiltWorker
class CheckInputsToSynchronizeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val packageInfoManager: IPackageInfoManager
) : CoroutineWorker(
    appContext,
    workerParams
) {
    override suspend fun doWork(): Result {
        val availableInputs = (packageInfoManager
            .getInstalledApplications()
            .firstOrNull()
            ?: emptyList())
            .map { packageInfo -> packageInfoManager.getInputsToSynchronize(packageInfo) }
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
                    NotificationCompat
                        .Builder(
                            applicationContext,
                            MainApplication.CHANNEL_CHECK_INPUTS_TO_SYNCHRONIZE
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
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
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
