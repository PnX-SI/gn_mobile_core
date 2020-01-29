package fr.geonature.sync.sync

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import fr.geonature.commons.data.DefaultNomenclature
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Nomenclature
import fr.geonature.commons.data.NomenclatureTaxonomy
import fr.geonature.commons.data.NomenclatureType
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.TaxonArea
import fr.geonature.commons.data.Taxonomy
import fr.geonature.sync.R
import fr.geonature.sync.api.GeoNatureAPIClient
import fr.geonature.sync.data.LocalDatabase
import fr.geonature.sync.sync.io.DatasetJsonReader
import fr.geonature.sync.sync.io.TaxonomyJsonReader
import fr.geonature.sync.util.SettingsUtils
import org.json.JSONObject
import retrofit2.Response
import java.util.Date

/**
 * Local data synchronization worker.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DataSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(
    appContext,
    workerParams
) {

    private val dataSyncManager = DataSyncManager.getInstance(applicationContext)

    override fun doWork(): Result {
        val startTime = Date()

        dataSyncManager.syncMessage.postValue(applicationContext.getString(R.string.sync_start_synchronization))

        val geoNatureServerUrl = SettingsUtils.getGeoNatureServerUrl(applicationContext)

        Log.i(
            TAG,
            "starting local data synchronization from '$geoNatureServerUrl'..."
        )

        if (geoNatureServerUrl.isNullOrBlank()) {
            dataSyncManager.syncMessage.postValue(applicationContext.getString(R.string.sync_error_server_url_configuration))
            Log.w(
                TAG,
                "No GeoNature server configured"
            )

            return Result.failure()
        }

        val geoNatureServiceClient = GeoNatureAPIClient.instance(
            applicationContext,
            geoNatureServerUrl
        )
            .value

        val syncDatasetResult = syncDataset(geoNatureServiceClient)

        if (syncDatasetResult is Result.Failure) {
            Log.i(
                TAG,
                "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms"
            )

            return syncDatasetResult
        }

        val syncInputObserversResult = syncInputObservers(geoNatureServiceClient)

        if (syncInputObserversResult is Result.Failure) {
            Log.i(
                TAG,
                "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms"
            )

            return syncInputObserversResult
        }

        val syncTaxonomyRanksResult = syncTaxonomyRanks(geoNatureServiceClient)

        if (syncTaxonomyRanksResult is Result.Failure) {
            Log.i(
                TAG,
                "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms"
            )

            return syncTaxonomyRanksResult
        }

        val syncTaxaResult = syncTaxa(geoNatureServiceClient)

        if (syncTaxaResult is Result.Failure) {
            Log.i(
                TAG,
                "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms"
            )

            return syncTaxaResult
        }

        val syncNomenclatureResult = syncNomenclature(geoNatureServiceClient)

        Log.i(
            TAG,
            "local data synchronization ${if (syncNomenclatureResult is Result.Success) "successfully finished" else "finished with failed tasks"} in ${Date().time - startTime.time}ms"
        )

        if (syncNomenclatureResult is Result.Success) {
            dataSyncManager.updateLastSynchronizedDate()
            dataSyncManager.syncMessage.postValue(applicationContext.getString(R.string.sync_data_succeeded))
        }

        return syncInputObserversResult
    }

    private fun syncDataset(geoNatureServiceClient: GeoNatureAPIClient): Result {
        return try {
            val response = geoNatureServiceClient.getMetaDatasets()
                .execute()

            checkResponse(response).run {
                if (this is Result.Failure) {
                    return this
                }
            }

            val jsonString = response.body()?.string() ?: return Result.failure()
            val dataset = DatasetJsonReader().read(jsonString)

            dataSyncManager.syncMessage.postValue(
                applicationContext.getString(
                    R.string.sync_data_dataset,
                    dataset.size
                )
            )

            Log.i(
                TAG,
                "dataset to update: ${dataset.size}"
            )

            LocalDatabase.getInstance(applicationContext)
                .datasetDao()
                .insert(*dataset.toTypedArray())

            Result.success()
        } catch (e: Exception) {
            dataSyncManager.syncMessage.postValue(applicationContext.getString(R.string.sync_error_server_error))

            Result.failure()
        }
    }

    private fun syncInputObservers(geoNatureServiceClient: GeoNatureAPIClient): Result {
        return try {
            val response = geoNatureServiceClient.getUsers()
                .execute()

            checkResponse(response).run {
                if (this is Result.Failure) {
                    return this
                }
            }

            val users = response.body() ?: return Result.failure()
            val inputObservers = users.map {
                InputObserver(
                    it.id,
                    it.lastname,
                    it.firstname
                )
            }
                .toTypedArray()

            dataSyncManager.syncMessage.postValue(
                applicationContext.getString(
                    R.string.sync_data_observers,
                    users.size
                )
            )

            Log.i(
                TAG,
                "users to update: ${users.size}"
            )

            LocalDatabase.getInstance(applicationContext)
                .inputObserverDao()
                .insert(*inputObservers)

            Result.success()
        } catch (e: Exception) {
            dataSyncManager.syncMessage.postValue(applicationContext.getString(R.string.sync_error_server_error))

            Result.failure()
        }
    }

    private fun syncTaxonomyRanks(geoNatureServiceClient: GeoNatureAPIClient): Result {
        return try {
            val taxonomyRanksResponse = geoNatureServiceClient.getTaxonomyRanks()
                .execute()

            checkResponse(taxonomyRanksResponse).run {
                if (this is Result.Failure) {
                    return this
                }
            }

            val jsonString = taxonomyRanksResponse.body()?.string() ?: return Result.failure()
            val taxonomy = TaxonomyJsonReader().read(jsonString)

            Log.i(
                TAG,
                "taxonomy to update: ${taxonomy.size}"
            )

            LocalDatabase.getInstance(applicationContext)
                .taxonomyDao()
                .insert(*taxonomy.toTypedArray())

            Result.success()
        } catch (e: Exception) {
            dataSyncManager.syncMessage.postValue(applicationContext.getString(R.string.sync_error_server_error))

            Result.failure()
        }
    }

    private fun syncTaxa(geoNatureServiceClient: GeoNatureAPIClient): Result {
        return try {
            val taxrefResponse = geoNatureServiceClient.getTaxref()
                .execute()

            checkResponse(taxrefResponse).run {
                if (this is Result.Failure) {
                    return this
                }
            }

            val taxrefAreasResponse = geoNatureServiceClient.getTaxrefAreas()
                .execute()

            checkResponse(taxrefAreasResponse).run {
                if (this is Result.Failure) {
                    return this
                }
            }

            val taxref = taxrefResponse.body() ?: return Result.failure()
            val taxrefAreas = taxrefAreasResponse.body() ?: return Result.failure()

            val taxa = taxref.map {
                Taxon(
                    it.id,
                    it.name,
                    Taxonomy(
                        it.kingdom,
                        it.group
                    ),
                    null
                )
            }
                .toTypedArray()

            dataSyncManager.syncMessage.postValue(
                applicationContext.getString(
                    R.string.sync_data_taxa,
                    taxa.size
                )
            )

            Log.i(
                TAG,
                "taxa to update: ${taxa.size}"
            )

            LocalDatabase.getInstance(applicationContext)
                .taxonDao()
                .insert(*taxa)

            val taxonAreas = taxrefAreas.asSequence()
                .filter { taxrefArea -> taxa.any { it.id == taxrefArea.taxrefId } }
                .map {
                    TaxonArea(
                        it.taxrefId,
                        it.areaId,
                        it.color,
                        it.numberOfObservers,
                        it.lastUpdatedAt
                    )
                }
                .toList()
                .toTypedArray()

            Log.i(
                TAG,
                "taxa with areas to update: ${taxonAreas.size}"
            )

            LocalDatabase.getInstance(applicationContext)
                .taxonAreaDao()
                .insert(*taxonAreas)

            Result.success()
        } catch (e: Exception) {
            dataSyncManager.syncMessage.postValue(applicationContext.getString(R.string.sync_error_server_error))

            Result.failure()
        }
    }

    private fun syncNomenclature(geoNatureServiceClient: GeoNatureAPIClient): Result {
        return try {
            val nomenclatureResponse = geoNatureServiceClient.getNomenclatures()
                .execute()

            checkResponse(nomenclatureResponse).run {
                if (this is Result.Failure) {
                    return this
                }
            }

            val nomenclatureTypes = nomenclatureResponse.body() ?: return Result.failure()
            val validNomenclatureTypesToUpdate = nomenclatureTypes.asSequence()
                .filter { it.id > 0 }
                .filter { it.nomenclatures.isNotEmpty() }

            val nomenclatureTypesToUpdate = validNomenclatureTypesToUpdate.map {
                NomenclatureType(
                    it.id,
                    it.mnemonic,
                    it.defaultLabel
                )
            }
                .toList()
                .toTypedArray()

            Log.i(
                TAG,
                "nomenclature types to update: ${nomenclatureTypesToUpdate.size}"
            )
            dataSyncManager.syncMessage.postValue(
                applicationContext.getString(
                    R.string.sync_data_nomenclature_type,
                    nomenclatureTypesToUpdate.size
                )
            )

            val nomenclaturesToUpdate = validNomenclatureTypesToUpdate.map { nomenclatureType ->
                nomenclatureType.nomenclatures.asSequence()
                    .filter { it.id > 0 }
                    .map {
                        Nomenclature(
                            it.id,
                            it.code,
                            if (TextUtils.isEmpty(it.hierarchy)) nomenclatureType.id.toString() else it.hierarchy!!,
                            it.defaultLabel,
                            nomenclatureType.id
                        )
                    }
            }
                .flatMap { it.asSequence() }
                .toList()
                .toTypedArray()

            val nomenclaturesTaxonomyToUpdate =
                validNomenclatureTypesToUpdate.map { it.nomenclatures }
                    .flatMap { it.asSequence() }
                    .filter { it.id > 0 }
                    .map { nomenclature ->
                        (if (nomenclature.taxref.isEmpty()) arrayOf(
                            fr.geonature.sync.api.model.NomenclatureTaxonomy(
                                Taxonomy.ANY,
                                Taxonomy.ANY
                            )
                        )
                        else nomenclature.taxref.toTypedArray()).asSequence()
                            .map {
                                Taxonomy(
                                    it.kingdom,
                                    it.group
                                )
                            }
                            .distinct()
                            .map {
                                NomenclatureTaxonomy(
                                    nomenclature.id,
                                    it
                                )
                            }
                    }
                    .flatMap { it.asSequence() }
                    .toList()
                    .toTypedArray()

            Log.i(
                TAG,
                "nomenclature to update: ${nomenclaturesToUpdate.size}"
            )
            dataSyncManager.syncMessage.postValue(
                applicationContext.getString(
                    R.string.sync_data_nomenclature,
                    nomenclaturesToUpdate.size
                )
            )

            // TODO: fetch available GeoNature modules
            val defaultNomenclatureResponse =
                geoNatureServiceClient.getDefaultNomenclaturesValues("occtax")
                    .execute()

            checkResponse(defaultNomenclatureResponse).run {
                if (this is Result.Failure) {
                    return this
                }
            }

            val jsonString = defaultNomenclatureResponse.body()?.string() ?: return Result.failure()
            val defaultNomenclatureAsJson = JSONObject(jsonString)
            val defaultNomenclaturesToUpdate = defaultNomenclatureAsJson.keys()
                .asSequence()
                .filter { mnemonic ->
                    nomenclatureTypesToUpdate.find { it.mnemonic == mnemonic } != null
                }
                .map {
                    DefaultNomenclature(
                        "occtax",
                        defaultNomenclatureAsJson.getLong(it)
                    )
                }
                .toList()
                .toTypedArray()

            Log.i(
                TAG,
                "nomenclature default values to update: ${defaultNomenclaturesToUpdate.size}"
            )
            dataSyncManager.syncMessage.postValue(
                applicationContext.getString(
                    R.string.sync_data_nomenclature_default,
                    defaultNomenclaturesToUpdate.size
                )
            )

            LocalDatabase.getInstance(applicationContext)
                .run {
                    this.nomenclatureTypeDao()
                        .insert(*nomenclatureTypesToUpdate)
                    this.nomenclatureDao()
                        .insert(*nomenclaturesToUpdate)
                    this.nomenclatureTaxonomyDao()
                        .insert(*nomenclaturesTaxonomyToUpdate)
                    this.defaultNomenclatureDao()
                        .insert(*defaultNomenclaturesToUpdate)
                }

            Result.success()
        } catch (e: Exception) {
            dataSyncManager.syncMessage.postValue(applicationContext.getString(R.string.sync_error_server_error))

            Result.failure()
        }
    }

    private fun checkResponse(response: Response<*>): Result {
        // not connected
        if (response.code() == 403) {
            dataSyncManager.serverStatus.postValue(ServerStatus.FORBIDDEN)
            dataSyncManager.syncMessage.postValue(applicationContext.getString(R.string.sync_error_server_not_connected))

            return Result.failure()
        }

        if (!response.isSuccessful) {
            dataSyncManager.serverStatus.postValue(ServerStatus.INTERNAL_SERVER_ERROR)
            dataSyncManager.syncMessage.postValue(applicationContext.getString(R.string.sync_error_server_error))

            return Result.failure()
        }

        return Result.success()
    }

    companion object {
        private val TAG = DataSyncWorker::class.java.name

        // The name of the synchronisation work
        const val DATA_SYNC_WORKER = "data_sync_worker"
        const val DATA_SYNC_WORKER_TAG = "data_sync_worker_tag"
    }
}
