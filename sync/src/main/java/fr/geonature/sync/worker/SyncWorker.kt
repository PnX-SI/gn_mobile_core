package fr.geonature.sync.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.geonature.commons.data.InputObserver
import fr.geonature.sync.api.GeoNatureAPIClient
import fr.geonature.sync.data.LocalDatabase

/**
 * Local data synchronisation worker.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class SyncWorker(appContext: Context,
                 workerParams: WorkerParameters) : Worker(appContext,
                                                          workerParams) {

    override fun doWork(): Result {
        val geoNatureServiceClient = GeoNatureAPIClient.instance.value
        val response = geoNatureServiceClient.getUsers()
            .execute()

        if (!response.isSuccessful) {
            return Result.failure()
        }

        val users = response.body() ?: return Result.failure()
        val inputObservers = users.map {
            InputObserver(it.id,
                          it.lastname,
                          it.firstname)
        }
            .toTypedArray()

        Log.i(TAG,
              "users to update: ${users.size}")

        LocalDatabase.getInstance(applicationContext)
            .inputObserverDao()
            .insert(*inputObservers)

        return Result.success()
    }

    companion object {
        private val TAG = SyncWorker::class.java.name
    }
}