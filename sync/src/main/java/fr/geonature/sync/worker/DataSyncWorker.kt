package fr.geonature.sync.worker

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.TaxonArea
import fr.geonature.sync.api.GeoNatureAPIClient
import fr.geonature.sync.data.LocalDatabase
import fr.geonature.sync.util.SettingsUtils

/**
 * Local data synchronisation worker.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DataSyncWorker(appContext: Context,
                     workerParams: WorkerParameters) : Worker(appContext,
                                                              workerParams) {

    override fun doWork(): Result {
        val geoNatureServerUrl = SettingsUtils.getGeoNatureServerUrl(applicationContext)

        if (TextUtils.isEmpty(geoNatureServerUrl)) {
            return Result.failure()
        }

        val geoNatureServiceClient = GeoNatureAPIClient.instance(geoNatureServerUrl!!)
            .value

        val syncInputObserversResult = syncInputObservers(geoNatureServiceClient)

        return if (syncInputObserversResult is Result.Success) syncTaxa(geoNatureServiceClient) else syncInputObserversResult
    }

    private fun syncInputObservers(geoNatureServiceClient: GeoNatureAPIClient): Result {
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

    private fun syncTaxa(geoNatureServiceClient: GeoNatureAPIClient): Result {
        val taxrefResponse = geoNatureServiceClient.getTaxref()
            .execute()

        if (!taxrefResponse.isSuccessful) {
            return Result.failure()
        }

        val taxrefAreasResponse = geoNatureServiceClient.getTaxrefAreas()
            .execute()

        if (!taxrefAreasResponse.isSuccessful) {
            return Result.failure()
        }

        val taxref = taxrefResponse.body() ?: return Result.failure()
        val taxrefAreas = taxrefAreasResponse.body() ?: return Result.failure()

        val taxa = taxref.map {
            Taxon(it.id,
                  it.name,
                  null)
        }
            .toTypedArray()

        Log.i(TAG,
              "taxa to update: ${taxa.size}")

        LocalDatabase.getInstance(applicationContext)
            .taxonDao()
            .insert(*taxa)

        val taxonAreas = taxrefAreas.asSequence()
            .filter { taxrefArea -> taxa.any { it.id == taxrefArea.taxrefId } }
            .map {
                TaxonArea(it.taxrefId,
                          it.areaId,
                          it.color,
                          it.numberOfObservers,
                          it.lastUpdatedAt)
            }
            .toList()
            .toTypedArray()

        Log.i(TAG,
              "taxa with areas to update: ${taxonAreas.size}")

        LocalDatabase.getInstance(applicationContext)
            .taxonAreaDao()
            .insert(*taxonAreas)

        return Result.success()
    }

    companion object {
        private val TAG = DataSyncWorker::class.java.name
    }
}