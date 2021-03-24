package fr.geonature.sync.sync.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import androidx.work.workDataOf
import fr.geonature.commons.data.DefaultNomenclature
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Nomenclature
import fr.geonature.commons.data.NomenclatureTaxonomy
import fr.geonature.commons.data.NomenclatureType
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.TaxonArea
import fr.geonature.commons.data.Taxonomy
import fr.geonature.sync.MainApplication
import fr.geonature.sync.R
import fr.geonature.sync.api.GeoNatureAPIClient
import fr.geonature.sync.api.model.User
import fr.geonature.sync.data.LocalDatabase
import fr.geonature.sync.settings.AppSettings
import fr.geonature.sync.sync.DataSyncManager
import fr.geonature.sync.sync.ServerStatus
import fr.geonature.sync.sync.io.DatasetJsonReader
import fr.geonature.sync.sync.io.TaxonomyJsonReader
import fr.geonature.sync.ui.home.HomeActivity
import org.json.JSONObject
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.BufferedReader
import java.io.InputStream
import java.util.Date
import java.util.Locale

/**
 * Local data synchronization worker.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DataSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {
    private val dataSyncManager = DataSyncManager.getInstance(applicationContext)
    private val workManager = WorkManager.getInstance(applicationContext)

    override suspend fun doWork(): Result {
        val startTime = Date()

        val geoNatureAPIClient = GeoNatureAPIClient.instance(applicationContext)
            ?: return Result.failure(
                workData(applicationContext.getString(R.string.sync_error_server_url_configuration))
            )

        val alreadyScheduled = workManager
            .getWorkInfosByTag(DATA_SYNC_WORKER_TAG)
            .await()
            .any {
                it.id != id && it.state in arrayListOf(
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING
                )
            }

        if (alreadyScheduled) {
            Log.i(
                TAG,
                "already scheduled: abort"
            )

            workManager.cancelWorkById(id)

            return Result.retry()
        }

        Log.i(
            TAG,
            "starting local data synchronization from '${geoNatureAPIClient.geoNatureBaseUrl}'..."
        )

        setProgress(workData(applicationContext.getString(R.string.sync_start_synchronization)))
        sendNotification(applicationContext.getString(R.string.sync_start_synchronization))

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
            ),
            inputData.getBoolean(
                INPUT_WITH_ADDITIONAL_DATA,
                true
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

        NotificationManagerCompat
            .from(applicationContext)
            .cancel(SYNC_NOTIFICATION_ID)

        if (syncNomenclatureResult is Result.Success) {
            dataSyncManager.updateLastSynchronizedDate()
            return Result.success(workData(applicationContext.getString(R.string.sync_data_succeeded)))
        }

        return syncInputObserversResult
    }

    private suspend fun syncDataset(geoNatureServiceClient: GeoNatureAPIClient): Result {
        Log.i(
            TAG,
            "synchronize dataset..."
        )

        val result = runCatching {
            geoNatureServiceClient
                .getMetaDatasets()
                .awaitResponse()
        }
            .map {
                checkResponse(it).run {
                    if (this is Result.Failure) this else it
                        .body()
                        ?.byteStream()
                        ?: Result.failure(
                            workData(applicationContext.getString(R.string.sync_data_dataset_error))
                        )
                }
            }
            .getOrElse {
                Result.failure(workData(applicationContext.getString(R.string.sync_data_dataset_error)))
            }

        if (result is Result.Failure) {
            return result
        }

        val dataset = DatasetJsonReader().read(
            (result as InputStream)
                .bufferedReader()
                .use(BufferedReader::readText)
        )

        Log.i(
            TAG,
            "dataset to update: ${dataset.size}"
        )

        if (dataset.isEmpty()) {
            return Result.success()
        }

        setProgress(
            workData(
                applicationContext.getString(
                    R.string.sync_data_dataset,
                    dataset.size
                )
            )
        )
        sendNotification(
            applicationContext.getString(
                R.string.sync_data_dataset,
                dataset.size
            )
        )

        LocalDatabase
            .getInstance(applicationContext)
            .datasetDao()
            .run {
                deleteAll()
                insert(*dataset.toTypedArray())
            }

        return Result.success()
    }

    private suspend fun syncInputObservers(
        geoNatureServiceClient: GeoNatureAPIClient,
        menuId: Int
    ): Result {
        Log.i(
            TAG,
            "synchronize users..."
        )

        val result = runCatching {
            geoNatureServiceClient
                .getUsers(menuId)
                .awaitResponse()
        }
            .map {
                checkResponse(it).run {
                    if (this is Result.Failure) this else runCatching { it.body() }.getOrNull()
                        ?: emptyList<List<User>>()
                }
            }
            .getOrElse {
                Result.failure(workData(applicationContext.getString(R.string.sync_data_observers_error)))
            }

        if (result is Result.Failure) {
            return result
        }

        val inputObservers = (result as List<*>)
            .asSequence()
            .filterNotNull()
            .map {
                it as User
                InputObserver(
                    it.id,
                    it.lastname,
                    it.firstname
                )
            }
            .toList()
            .toTypedArray()

        Log.i(
            TAG,
            "users to update: ${inputObservers.size}"
        )

        if (inputObservers.isEmpty()) {
            return Result.success()
        }

        setProgress(
            workData(
                applicationContext.getString(
                    R.string.sync_data_observers,
                    inputObservers.size
                )
            )
        )
        sendNotification(
            applicationContext.getString(
                R.string.sync_data_observers,
                inputObservers.size
            )
        )

        LocalDatabase
            .getInstance(applicationContext)
            .inputObserverDao()
            .run {
                deleteAll()
                insert(*inputObservers)
            }

        return Result.success()
    }

    private suspend fun syncTaxonomyRanks(geoNatureServiceClient: GeoNatureAPIClient): Result {
        Log.i(
            TAG,
            "synchronize taxonomy ranks..."
        )

        val result = runCatching {
            geoNatureServiceClient
                .getTaxonomyRanks()
                .awaitResponse()
        }
            .map {
                checkResponse(it).run {
                    if (this is Result.Failure) this else it
                        .body()
                        ?.byteStream()
                        ?: Result.failure(
                            workData(
                                applicationContext.getString(R.string.sync_data_taxonomy_ranks_error)
                            )
                        )
                }
            }
            .getOrElse {
                Result.failure(workData(applicationContext.getString(R.string.sync_data_taxonomy_ranks_error)))
            }

        if (result is Result.Failure) {
            return result
        }

        val taxonomyRanks = TaxonomyJsonReader().read(
            (result as InputStream)
                .bufferedReader()
                .use(BufferedReader::readText)
        )

        Log.i(
            TAG,
            "taxonomy ranks to update: ${taxonomyRanks.size}"
        )

        if (taxonomyRanks.isEmpty()) {
            return Result.success()
        }

        setProgress(
            workData(
                applicationContext.getString(
                    R.string.sync_data_taxonomy_ranks,
                    taxonomyRanks.size
                )
            )
        )
        sendNotification(
            applicationContext.getString(
                R.string.sync_data_taxonomy_ranks,
                taxonomyRanks.size
            )
        )

        LocalDatabase
            .getInstance(applicationContext)
            .taxonomyDao()
            .run {
                deleteAll()
                insert(*taxonomyRanks.toTypedArray())
            }

        return Result.success()
    }

    private suspend fun syncTaxa(
        geoNatureServiceClient: GeoNatureAPIClient,
        listId: Int,
        codeAreaType: String?,
        pageSize: Int,
        pageMaxRetry: Int,
        withAdditionalData: Boolean = true
    ): Result {
        Log.i(
            TAG,
            "synchronize taxa..."
        )

        var hasNext: Boolean
        var offset = 0

        val validTaxaIds = mutableSetOf<Long>()

        // fetch all taxa from paginated list
        do {
            val taxrefResponse = runCatching {
                geoNatureServiceClient
                    .getTaxref(
                        listId,
                        pageSize,
                        offset
                    )
                    .awaitResponse()
            }.getOrNull()


            if (taxrefResponse == null || checkResponse(taxrefResponse) is Result.Failure) {
                hasNext = false
                continue
            }

            val taxref = runCatching { taxrefResponse.body() }.getOrDefault(emptyList())

            if (taxref?.isEmpty() != false) {
                hasNext = false
                continue
            }

            val taxa = taxref
                .asSequence()
                .map { taxRef ->
                    Taxon(taxRef.id,
                        taxRef.name.trim(),
                        Taxonomy(
                            taxRef.kingdom,
                            taxRef.group
                        ),
                        taxRef.commonName?.trim(),
                        taxRef.fullName.trim(),
                        ".+\\[(\\w+) - \\d+]"
                            .toRegex()
                            .find(taxRef.description)?.groupValues
                            ?.elementAtOrNull(1)
                            ?.let { "${it.toUpperCase(Locale.ROOT)} - ${taxRef.id}" })
                }
                .onEach {
                    validTaxaIds.add(it.id)
                }
                .toList()
                .toTypedArray()

            LocalDatabase
                .getInstance(applicationContext)
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
            sendNotification(
                applicationContext.getString(
                    R.string.sync_data_taxa,
                    (offset + taxa.size)
                )
            )

            if (taxa.size == pageSize) {
                offset += pageSize
                hasNext = offset / pageSize < pageMaxRetry
            } else {
                hasNext = false
            }
        } while (hasNext)

        // delete orphaned taxa
        LocalDatabase
            .getInstance(applicationContext)
            .taxonDao()
            .QB()
            .cursor()
            .run {
                Log.i(
                    TAG,
                    "deleting orphaned taxa..."
                )

                val orphanedTaxaIds = mutableSetOf<Long>()

                moveToFirst()

                while (!isAfterLast) {
                    Taxon
                        .fromCursor(this)
                        ?.run {
                            if (!validTaxaIds.contains(id)) {
                                LocalDatabase
                                    .getInstance(applicationContext)
                                    .taxonDao()
                                    .deleteById(id)
                                orphanedTaxaIds.add(id)
                            }
                        }

                    moveToNext()
                }

                Log.i(
                    TAG,
                    "orphaned taxa deleted: ${orphanedTaxaIds.size}"
                )
            }

        if (withAdditionalData) {
            Log.i(
                TAG,
                "synchronize taxa additional data..."
            )

            offset = 0

            // fetch all taxa metadata from paginated list
            do {
                val taxrefAreasResponse = runCatching {
                    geoNatureServiceClient
                        .getTaxrefAreas(
                            codeAreaType,
                            pageSize,
                            offset
                        )
                        .awaitResponse()
                }.getOrNull()

                if (taxrefAreasResponse == null || checkResponse(taxrefAreasResponse) is Result.Failure) {
                    hasNext = false
                    continue
                }

                val taxrefAreas = runCatching { taxrefAreasResponse.body() }.getOrDefault(emptyList())

                if (taxrefAreas?.isEmpty() != false) {
                    hasNext = false
                    continue
                }

                Log.i(
                    TAG,
                    "found ${taxrefAreas.size} taxa with areas from offset $offset"
                )

                setProgress(
                    workData(
                        applicationContext.getString(
                            R.string.sync_data_taxa_areas,
                            (offset + taxrefAreas.size)
                        )
                    )
                )
                sendNotification(
                    applicationContext.getString(
                        R.string.sync_data_taxa_areas,
                        (offset + taxrefAreas.size)
                    )
                )

                val taxonAreas = taxrefAreas
                    .asSequence()
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

                LocalDatabase
                    .getInstance(applicationContext)
                    .taxonAreaDao()
                    .insert(*taxonAreas)

                Log.i(
                    TAG,
                    "updating ${taxonAreas.size} taxa with areas from offset $offset"
                )

                offset += pageSize
                hasNext = offset / pageSize < pageMaxRetry
            } while (hasNext)
        }

        return Result.success()
    }

    private suspend fun syncNomenclature(geoNatureServiceClient: GeoNatureAPIClient): Result {
        Log.i(
            TAG,
            "synchronize nomenclature types..."
        )

        val nomenclaturesResult = runCatching {
            geoNatureServiceClient
                .getNomenclatures()
                .awaitResponse()
        }
            .map {
                checkResponse(it).run {
                    if (this is Result.Failure) this else runCatching { it.body() }.getOrNull()
                        ?: emptyList<fr.geonature.sync.api.model.NomenclatureType>()
                }
            }
            .getOrElse {
                Result.failure(workData(applicationContext.getString(R.string.sync_data_nomenclature_type_error)))
            }

        if (nomenclaturesResult is Result.Failure) {
            return nomenclaturesResult
        }

        val validNomenclatureTypesToUpdate = (nomenclaturesResult as List<*>)
            .asSequence()
            .filterNotNull()
            .map { it as fr.geonature.sync.api.model.NomenclatureType }
            .filter { it.id > 0 }
            .filter { it.nomenclatures.isNotEmpty() }

        val nomenclatureTypesToUpdate = validNomenclatureTypesToUpdate
            .map {
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

        if (nomenclatureTypesToUpdate.isEmpty()) {
            return Result.success()
        }

        setProgress(
            workData(
                applicationContext.getString(
                    R.string.sync_data_nomenclature_type,
                    nomenclatureTypesToUpdate.size
                )
            )
        )
        sendNotification(
            applicationContext.getString(
                R.string.sync_data_nomenclature_type,
                nomenclatureTypesToUpdate.size
            )
        )

        val nomenclaturesToUpdate = validNomenclatureTypesToUpdate
            .map { nomenclatureType ->
                nomenclatureType.nomenclatures
                    .asSequence()
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

        val nomenclaturesTaxonomyToUpdate = validNomenclatureTypesToUpdate
            .map { it.nomenclatures }
            .flatMap { it.asSequence() }
            .filter { it.id > 0 }
            .map { nomenclature ->
                (if (nomenclature.taxref.isEmpty()) arrayOf(
                    fr.geonature.sync.api.model.NomenclatureTaxonomy(
                        Taxonomy.ANY,
                        Taxonomy.ANY
                    )
                )
                else nomenclature.taxref.toTypedArray())
                    .asSequence()
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

        val taxonomyToUpdate = nomenclaturesTaxonomyToUpdate
            .asSequence()
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
        sendNotification(
            applicationContext.getString(
                R.string.sync_data_nomenclature,
                nomenclaturesToUpdate.size
            )
        )

        Log.i(
            TAG,
            "synchronize nomenclature default values..."
        )

        // TODO: fetch available GeoNature modules
        val defaultNomenclatureResult = runCatching {
            geoNatureServiceClient
                .getDefaultNomenclaturesValues("occtax")
                .awaitResponse()
        }
            .map {
                checkResponse(it).run {
                    if (this is Result.Failure) this else it
                        .body()
                        ?.byteStream()
                        ?: Result.failure(workData(applicationContext.getString(R.string.sync_data_nomenclature_default_error)))
                }
            }
            .getOrElse {
                Result.failure(workData(applicationContext.getString(R.string.sync_data_nomenclature_default_error)))
            }

        if (nomenclaturesResult is Result.Failure) {
            return nomenclaturesResult
        }

        val defaultNomenclatureAsJson = runCatching {
            JSONObject(
                (defaultNomenclatureResult as InputStream)
                    .bufferedReader()
                    .use(BufferedReader::readText)
            )
        }.getOrNull()
            ?: JSONObject()

        val defaultNomenclaturesToUpdate = defaultNomenclatureAsJson
            .keys()
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
        sendNotification(
            applicationContext.getString(
                R.string.sync_data_nomenclature_default,
                defaultNomenclaturesToUpdate.size
            )
        )

        LocalDatabase
            .getInstance(applicationContext)
            .run {
                nomenclatureTypeDao().run {
                    deleteAll()
                    insert(*nomenclatureTypesToUpdate)
                }
                nomenclatureDao().run {
                    deleteAll()
                    insert(*nomenclaturesToUpdate)
                }
                taxonomyDao().insertOrIgnore(*taxonomyToUpdate)
                nomenclatureTaxonomyDao().insert(*nomenclaturesTaxonomyToUpdate)
                defaultNomenclatureDao().insert(*defaultNomenclaturesToUpdate)
            }

        return Result.success()
    }

    private fun sendNotification(contentText: CharSequence) {
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(
                SYNC_NOTIFICATION_ID,
                NotificationCompat
                    .Builder(
                        applicationContext,
                        MainApplication.CHANNEL_DATA_SYNCHRONIZATION
                    )
                    .setContentTitle(applicationContext.getText(R.string.notification_data_synchronization_title))
                    .setContentText(contentText)
                    .setContentIntent(
                        PendingIntent.getActivity(
                            applicationContext,
                            0,
                            Intent(
                                applicationContext,
                                HomeActivity::class.java
                            ).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            },
                            0
                        )
                    )
                    .setSmallIcon(R.drawable.ic_sync)
                    .build()
            )
        }
    }

    private fun checkResponse(response: Response<*>): Result {
        // not connected
        if (response.code() == ServerStatus.UNAUTHORIZED.httpStatus) {
            return Result.failure(
                workData(
                    applicationContext.getString(R.string.sync_error_server_not_connected),
                    ServerStatus.UNAUTHORIZED
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

    private fun workData(
        syncMessage: String,
        serverStatus: ServerStatus = ServerStatus.OK
    ): Data {
        return workDataOf(
            KEY_SYNC_MESSAGE to syncMessage,
            KEY_SERVER_STATUS to serverStatus.ordinal
        )
    }

    companion object {
        private val TAG = DataSyncWorker::class.java.name

        const val KEY_SYNC_MESSAGE = "KEY_SYNC_MESSAGE"
        const val KEY_SERVER_STATUS = "KEY_SERVER_STATUS"

        // the name of the synchronization work
        const val DATA_SYNC_WORKER = "data_sync_worker"
        const val DATA_SYNC_WORKER_PERIODIC = "data_sync_worker_periodic"
        const val DATA_SYNC_WORKER_PERIODIC_ESSENTIAL = "data_sync_worker_periodic_essential"
        const val DATA_SYNC_WORKER_TAG = "data_sync_worker_tag"

        private const val SYNC_NOTIFICATION_ID = 3

        private const val INPUT_USERS_MENU_ID = "usersMenuId"
        private const val INPUT_TAXREF_LIST_ID = "taxrefListId"
        private const val INPUT_CODE_AREA_TYPE = "codeAreaType"
        private const val INPUT_PAGE_SIZE = "pageSize"
        private const val INPUT_PAGE_MAX_RETRY = "pageMaxRetry"
        private const val INPUT_WITH_ADDITIONAL_DATA = "withAdditionalData"

        /**
         * Configure input data to this [DataSyncWorker] from given [AppSettings].
         */
        fun inputData(
            appSettings: AppSettings,
            withAdditionalData: Boolean = true
        ): Data {
            return Data
                .Builder()
                .putAll(
                    mapOf(
                        Pair(
                            INPUT_USERS_MENU_ID,
                            appSettings.usersListId
                        ),
                        Pair(
                            INPUT_TAXREF_LIST_ID,
                            appSettings.taxrefListId
                        ),
                        Pair(
                            INPUT_CODE_AREA_TYPE,
                            appSettings.codeAreaType
                        ),
                        Pair(
                            INPUT_PAGE_SIZE,
                            appSettings.pageSize
                        ),
                        Pair(
                            INPUT_PAGE_MAX_RETRY,
                            appSettings.pageMaxRetry
                        ),
                        Pair(
                            INPUT_WITH_ADDITIONAL_DATA,
                            withAdditionalData
                        )
                    )
                )
                .build()
        }
    }
}
