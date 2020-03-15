package fr.geonature.sync

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import fr.geonature.commons.util.DeviceUtils
import fr.geonature.mountpoint.util.MountPointUtils.getExternalStorage
import fr.geonature.mountpoint.util.MountPointUtils.getInternalStorage
import fr.geonature.sync.sync.CheckInputsToSynchronizeWorker
import java.util.concurrent.TimeUnit

/**
 * Base class to maintain global application state.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.i(
            TAG,
            "internal storage: " + getInternalStorage()
        )
        Log.i(
            TAG,
            "external storage: " + getExternalStorage(this)
        )

        checkInputsToSynchronize()
    }

    private fun checkInputsToSynchronize() {
        val notificationManager = NotificationManagerCompat.from(this)
        configureSyncChannel(notificationManager)
        val workManager: WorkManager = WorkManager.getInstance(this)

        val request = PeriodicWorkRequestBuilder<CheckInputsToSynchronizeWorker>(
            15,
            TimeUnit.MINUTES
        ).addTag(CheckInputsToSynchronizeWorker.CHECK_INPUTS_TO_SYNC_WORKER_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            CheckInputsToSynchronizeWorker.CHECK_INPUTS_TO_SYNC_WORKER,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    private fun configureSyncChannel(notificationManager: NotificationManagerCompat): NotificationChannel? {
        if (DeviceUtils.isPostOreo) {
            val channel = NotificationChannel(
                SYNC_CHANNEL_ID,
                getText(R.string.sync_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.sync_channel_description)
                setShowBadge(true)
            }

            // register this channel with the system
            notificationManager.createNotificationChannel(channel)

            return channel
        }

        return null
    }

    companion object {
        private val TAG = MainApplication::class.java.name

        const val SYNC_CHANNEL_ID = "sync_channel"
    }
}
