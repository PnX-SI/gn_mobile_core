package fr.geonature.sync.sync.worker

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
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
import fr.geonature.sync.settings.AppSettings
import fr.geonature.sync.sync.DataSyncManager
import fr.geonature.sync.sync.ServerStatus
import fr.geonature.sync.sync.io.DatasetJsonReader
import fr.geonature.sync.sync.io.TaxonomyJsonReader
import org.json.JSONObject
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.BufferedReader
import java.util.Date

/**
 * Local data synchronization worker.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DataSyncWorker(
    appContext: Context, workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {
    private val dataSyncManager =
        DataSyncManager.getInstance(applicationContext)

    override suspend fun doWork(): Result {
        val startTime = Date()

        val geoNatureAPIClient = GeoNatureAPIClient.instance(applicationContext)
            ?: return Result.failure(workData(applicationContext.getString(R.string.sync_error_server_url_configuration)))

        Log.i(
            TAG,
            "starting local data synchronization from '${geoNatureAPIClient.geoNatureBaseUrl}'..."
        )

        setProgress(workData(applicationContext.getString(R.string.sync_start_synchronization)))

        val syncDatasetResult = syncDataset(geoNatureAPIClient)

        if (syncDatasetResult is Result.Failure) {
            Log.i(
                TAG,
                "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms"
            )

            return syncDatasetResult
        }

        val syncInputObserversResult = syncInputObservers(
            geoNatureAPIClient,
            inputData.getInt(
                INPUT_USERS_MENU_ID,
                0
            )
        )

        if (syncInputObserversResult is Result.Failure) {
            Log.i(
                TAG,
                "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms"
            )

            return syncInputObserversResult
        }

        val syncTaxonomyRanksResult = syncTaxonomyRanks(geoNatureAPIClient)

        if (syncTaxonomyRanksResult is Result.Failure) {
            Log.i(
                TAG,
                "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms"
            )

            return syncTaxonomyRanksResult
        }

        val syncTaxaResult = syncTaxa(
            geoNatureAPIClient,
            inputData.getInt(
                INPUT_TAXREF_LIST_ID,
                0
            ),
            inputData.getString(INPUT_CODE_AREA_TYPE),
            inputData.getInt(
                INPUT_PAGE_SIZE,
                AppSettings.DEFAULT_PAGE_SIZE
            ),
            inputData.getInt(
                INPUT_PAGE_MAX_RETRY,
                AppSettings.DEFAULT_PAGE_MAX_RETRY
            )
        )

        if (syncTaxaResult is Result.Failure) {
            Log.i(
                TAG,
                "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms"
            )

            return syncTaxaResult
        }

        val syncNomenclatureResult = syncNomenclature(geoNatureAPIClient)

        Log.i(
            TAG,
            "local data synchronization ${if (syncNomenclatureResult is Result.Success) "successfully finished" else "finished with failed tasks"} in ${Date().time - startTime.time}ms"
        )

        if (syncNomenclatureResult is Result.Success) {
            dataSyncManager.updateLastSynchronizedDate()
            return Result.success(workData(applicationContext.getString(R.string.sync_data_succeeded)))
        }

        return syncInputObserversResult
    }

    private suspend fun syncDataset(geoNatureServiceClient: GeoNatureAPIClient): Result {
        return try {
            val response = geoNatureServiceClient.getMetaDatasets()
                .awaitResponse()

            checkResponse(response).run {
                if (this is Result.Failure) {
                    return this
                }
            }

            val inputStream = response.body()
                ?.byteStream() ?: return Result.failure()
            val dataset = DatasetJsonReader().read(
                inputStream.bufferedReader()
                    .use(BufferedReader::readText)
            )

            Log.i(
                TAG,
                "dataset to update: ${dataset.size}"
            )

            setProgress(
                workData(
                    applicationContext.getString(
                        R.string.sync_data_dataset,
                        dataset.size
                    )
                )
            )

            LocalDatabase.getInstance(applicationContext)
                .datasetDao()
                .insert(*dataset.toTypedArray())

            Result.success()
        } catch (e: Exception) {
            Result.failure(
                workData(
                    applicationContext.getString(R.string.sync_error_server_error)
                )
            )
        }
    }

    private suspend fun syncInputObservers(
        geoNatureServiceClient: GeoNatureAPIClient, menuId: Int
    ): Result {
        return try {
            val response = geoNatureServiceClient.getUsers(menuId)
                .awaitResponse()

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

            Log.i(
                TAG,
                "users to update: ${users.size}"
            )

            setProgress(
                workData(
                    applicationContext.getString(
                        R.string.sync_data_observers,
                        users.size
                    )
                )
            )

            LocalDatabase.getInstance(applicationContext)
                .inputObserverDao()
                .insert(*inputObservers)

            Result.success()
        } catch (e: Exception) {
            Result.failure(
                workData(
                    applicationContext.getString(R.string.sync_error_server_error)
                )
            )
        }
    }

    private suspend fun syncTaxonomyRanks(geoNatureServiceClient: GeoNatureAPIClient): Result {
        return try {
            val taxonomyRanksResponse = geoNatureServiceClient.getTaxonomyRanks()
                .awaitResponse()

            checkResponse(taxonomyRanksResponse).run {
                if (this is Result.Failure) {
                    return this
                }
            }

            val inputStream = taxonomyRanksResponse.body()
                ?.byteStream() ?: return Result.failure()
            val taxonomy = TaxonomyJsonReader().read(
                inputStream.bufferedReader()
                    .use(BufferedReader::readText)
            )

            Log.i(
                TAG,
                "taxonomy to update: ${taxonomy.size}"
            )

            LocalDatabase.getInstance(applicationContext)
                .taxonomyDao()
                .insert(*taxonomy.toTypedArray())

            Result.success()
        } catch (e: Exception) {
            Result.failure(
                workData(
                    applicationContext.getString(R.string.sync_error_server_error)
                )
            )
        }
    }

    private suspend fun syncTaxa(
        geoNatureServiceClient: GeoNatureAPIClient,
        listId: Int,
        codeAreaType: String?,
        pageSize: Int,
        pageMaxRetry: Int
    ): Result {
        return try {
            var hasNext: Boolean
            var offset = 0

            val validTaxaIds = mutableSetOf<Long>()

            // fetch all taxa from paginated list
            do {
                val taxrefResponse = geoNatureServiceClient.getTaxref(
                    listId,
                    pageSize,
                    offset
                )
                    .awaitResponse()

                if (checkResponse(taxrefResponse) is Result.Failure) {
                    hasNext = false
                    continue
                }

                val taxref = taxrefResponse.body()

                if (taxref == null || taxref.isEmpty()) {
                    hasNext = false
                    continue
                }

                val taxa = taxref.asSequence()
                    .map {
                        Taxon(
                            it.id,
                            it.name,
                            Taxonomy(
                                it.kingdom,
                                it.group
                            ),
                            it.description
                        )
                    }
                    .onEach {
                        validTaxaIds.add(it.id)
                    }
                    .toList()
                    .toTypedArray()

                LocalDatabase.getInstance(applicationContext)
                    .taxonDao()
                    .insert(*taxa)

                Log.i(
                    TAG,
                    "taxa to update: ${offset + taxa.size}"
                )

                setProgress(
                    workData(
                        applicationContext.getString(
                            R.string.sync_data_taxa,
                            (offset + taxa.size)
                        )
                    )
                )

                if (taxa.size == pageSize) {
                    offset += pageSize
                    hasNext = offset / pageSize < pageMaxRetry
                } else {
                    hasNext = false
                }
            } while (hasNext)

            offset = 0

            // fetch all taxa metadata from paginated list
            do {
                val taxrefAreasResponse = geoNatureServiceClient.getTaxrefAreas(
                    codeAreaType,
                    pageSize,
                    offset
                )
                    .awaitResponse()

                if (checkResponse(taxrefAreasResponse) is Result.Failure) {
                    hasNext = false
                    continue
                }

                val taxrefAreas = taxrefAreasResponse.body()

                if (taxrefAreas == null || taxrefAreas.isEmpty()) {
                    hasNext = false
                    continue
                }

                Log.i(
                    TAG,
                    "found ${taxrefAreas.size} taxa with areas from offset $offset"
                )

                val taxonAreas = taxrefAreas.asSequence()
                    .filter { taxrefArea -> validTaxaIds.any { it == taxrefArea.taxrefId } }
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

                LocalDatabase.getInstance(applicationContext)
                    .taxonAreaDao()
                    .insert(*taxonAreas)

                Log.i(
                    TAG,
                    "updating ${taxonAreas.size} taxa with areas from offset $offset"
                )

                offset += pageSize
                hasNext = offset / pageSize < pageMaxRetry
            } while (hasNext)

            Result.success()
        } catch (e: Exception) {
            Log.w(
                TAG,
                e
            )

            Result.failure(
                workData(
                    applicationContext.getString(R.string.sync_error_server_error)
                )
            )
        }
    }

    private suspend fun syncNomenclature(geoNatureServiceClient: GeoNatureAPIClient): Result {
        return try {
            val nomenclatureResponse = geoNatureServiceClient.getNomenclatures()
                .awaitResponse()

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

            setProgress(
                workData(
                    applicationContext.getString(
                        R.string.sync_data_nomenclature_type,
                        nomenclatureTypesToUpdate.size
                    )
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

            val taxonomyToUpdate = nomenclaturesTaxonomyToUpdate.asSequence()
                .map { it.taxonomy }
                .toList()
                .toTypedArray()

            setProgress(
                workData(
                    applicationContext.getString(
                        R.string.sync_data_nomenclature,
                        nomenclaturesToUpdate.size
                    )
                )
            )

            // TODO: fetch available GeoNature modules
            val defaultNomenclatureResponse =
                geoNatureServiceClient.getDefaultNomenclaturesValues("occtax")
                    .awaitResponse()

            checkResponse(defaultNomenclatureResponse).run {
                if (this is Result.Failure) {
                    return this
                }
            }

            val inputStream = defaultNomenclatureResponse.body()
                ?.byteStream() ?: return Result.failure()
            val defaultNomenclatureAsJson = JSONObject(
                inputStream.bufferedReader()
                    .use(BufferedReader::readText)
            )
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

            setProgress(
                workData(
                    applicationContext.getString(
                        R.string.sync_data_nomenclature_default,
                        defaultNomenclaturesToUpdate.size
                    )
                )
            )

            LocalDatabase.getInstance(applicationContext)
                .run {
                    this.nomenclatureTypeDao()
                        .insert(*nomenclatureTypesToUpdate)
                    this.nomenclatureDao()
                        .insert(*nomenclaturesToUpdate)
                    this.taxonomyDao()
                        .insertOrIgnore(*taxonomyToUpdate)
                    this.nomenclatureTaxonomyDao()
                        .insert(*nomenclaturesTaxonomyToUpdate)
                    this.defaultNomenclatureDao()
                        .insert(*defaultNomenclaturesToUpdate)
                }

            Result.success()
        } catch (e: Exception) {
            Result.failure(
                workData(
                    applicationContext.getString(R.string.sync_error_server_error)
                )
            )
        }
    }

    private fun checkResponse(response: Response<*>): Result {
        // not connected
        if (response.code() == 403) {
            return Result.failure(
                workData(
                    applicationContext.getString(R.string.sync_error_server_not_connected),
                    ServerStatus.FORBIDDEN
                )
            )
        }

        if (!response.isSuccessful) {
            return Result.failure(
                workData(
                    applicationContext.getString(R.string.sync_error_server_error),
                    ServerStatus.INTERNAL_SERVER_ERROR
                )
            )
        }

        return Result.success()
    }

    private fun workData(syncMessage: String, serverStatus: ServerStatus = ServerStatus.OK): Data {
        return workDataOf(
            KEY_SYNC_MESSAGE to syncMessage,
            KEY_SERVER_STATUS to serverStatus.ordinal
        )
    }

    companion object {
        private val TAG = DataSyncWorker::class.java.name

        const val KEY_SYNC_MESSAGE = "KEY_SYNC_MESSAGE"
        const val KEY_SERVER_STATUS = "KEY_SERVER_STATUS"

        // The name of the synchronization work
        const val DATA_SYNC_WORKER = "data_sync_worker"
        const val DATA_SYNC_WORKER_TAG = "data_sync_worker_tag"

        const val INPUT_USERS_MENU_ID = "usersMenuId"
        const val INPUT_TAXREF_LIST_ID = "taxrefListId"
        const val INPUT_CODE_AREA_TYPE = "codeAreaType"
        const val INPUT_PAGE_SIZE = "pageSize"
        const val INPUT_PAGE_MAX_RETRY = "pageMaxRetry"
    }
}
