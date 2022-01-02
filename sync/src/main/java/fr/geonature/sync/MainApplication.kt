package fr.geonature.sync

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import fr.geonature.mountpoint.util.MountPointUtils
import fr.geonature.sync.di.ServiceLocator
import fr.geonature.sync.sync.worker.CheckAuthLoginWorker
import fr.geonature.sync.sync.worker.CheckInputsToSynchronizeWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Base class to maintain global application state.
 *
 * @author S. Grimault
 */
@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private fun checkAuthLogin() {
        val workManager: WorkManager = WorkManager.getInstance(this)

        val request = PeriodicWorkRequestBuilder<CheckAuthLoginWorker>(
            1,
            TimeUnit.HOURS
        )
            .addTag(CheckAuthLoginWorker.CHECK_AUTH_LOGIN_WORKER_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            CheckAuthLoginWorker.CHECK_AUTH_LOGIN_WORKER,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    private fun checkInputsToSynchronize() {
        val workManager: WorkManager = WorkManager.getInstance(this)

        val request = PeriodicWorkRequestBuilder<CheckInputsToSynchronizeWorker>(
            15,
            TimeUnit.MINUTES
        )
            .addTag(CheckInputsToSynchronizeWorker.CHECK_INPUTS_TO_SYNC_WORKER_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            CheckInputsToSynchronizeWorker.CHECK_INPUTS_TO_SYNC_WORKER,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    private fun configureCheckInputsToSynchronizeChannel(notificationManager: NotificationManagerCompat): NotificationChannel? {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_CHECK_INPUTS_TO_SYNCHRONIZE,
                getText(R.string.channel_name_check_inputs_to_synchronize),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description_check_inputs_to_synchronize)
                setShowBadge(true)
            }

            // register this channel with the system
            notificationManager.createNotificationChannel(channel)

            return channel
        }

        return null
    }

    private fun configureSynchronizeDataChannel(notificationManager: NotificationManagerCompat): NotificationChannel? {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_DATA_SYNCHRONIZATION,
                getText(R.string.channel_name_data_synchronization),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description_data_synchronization)
            }

            // register this channel with the system
            notificationManager.createNotificationChannel(channel)

            return channel
        }

        return null
    }

    @Deprecated("use instead Hilt as default dependency injection")
    val sl = ServiceLocator(this)

    override fun onCreate() {
        super.onCreate()

        Log.i(
            TAG,
            "internal storage: " + MountPointUtils.getInternalStorage(this)
        )
        Log.i(
            TAG,
            "external storage: " + MountPointUtils.getExternalStorage(this)
        )

        val notificationManager = NotificationManagerCompat.from(this)
        configureCheckInputsToSynchronizeChannel(notificationManager)
        configureSynchronizeDataChannel(notificationManager)

        checkAuthLogin()
        checkInputsToSynchronize()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration
            .Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    companion object {
        private val TAG = MainApplication::class.java.name

        const val CHANNEL_CHECK_INPUTS_TO_SYNCHRONIZE = "channel_check_inputs_to_synchronize"
        const val CHANNEL_DATA_SYNCHRONIZATION = "channel_data_synchronization"
    }
}
