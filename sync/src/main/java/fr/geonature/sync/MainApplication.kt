package fr.geonature.sync

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import fr.geonature.datasync.auth.worker.CheckAuthLoginWorker
import fr.geonature.datasync.packageinfo.worker.CheckInputsToSynchronizeWorker
import fr.geonature.mountpoint.util.MountPointUtils
import fr.geonature.sync.ui.home.HomeActivity
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
        CheckAuthLoginWorker.enqueueUniquePeriodicWork(this)
    }

    private fun checkInputsToSynchronize() {
        CheckInputsToSynchronizeWorker.enqueueUniquePeriodicWork(
            this,
            HomeActivity::class.java.name
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
