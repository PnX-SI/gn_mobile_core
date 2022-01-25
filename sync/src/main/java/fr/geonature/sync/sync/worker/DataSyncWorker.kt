package fr.geonature.sync.sync.worker

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.geonature.commons.data.dao.DatasetDao
import fr.geonature.commons.data.dao.DefaultNomenclatureDao
import fr.geonature.commons.data.dao.InputObserverDao
import fr.geonature.commons.data.dao.NomenclatureDao
import fr.geonature.commons.data.dao.NomenclatureTaxonomyDao
import fr.geonature.commons.data.dao.NomenclatureTypeDao
import fr.geonature.commons.data.dao.TaxonAreaDao
import fr.geonature.commons.data.dao.TaxonDao
import fr.geonature.commons.data.dao.TaxonomyDao
import fr.geonature.commons.data.entity.DefaultNomenclature
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureTaxonomy
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.api.model.User
import fr.geonature.datasync.auth.IAuthManager
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.ui.login.LoginActivity
import fr.geonature.sync.MainApplication
import fr.geonature.sync.R
import fr.geonature.sync.sync.DataSyncManager
import fr.geonature.sync.sync.DataSyncStatus
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
 * @author S. Grimault
 */
@HiltWorker
class DataSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val authManager: IAuthManager,
    private val geoNatureAPIClient: IGeoNatureAPIClient,
    private val datasetDao: DatasetDao,
    private val inputObserverDao: InputObserverDao,
    private val taxonomyDao: TaxonomyDao,
    private val taxonDao: TaxonDao,
    private val taxonAreaDao: TaxonAreaDao,
    private val nomenclatureTypeDao: NomenclatureTypeDao,
    private val nomenclatureDao: NomenclatureDao,
    private val nomenclatureTaxonomyDao: NomenclatureTaxonomyDao,
    private val defaultNomenclatureDao: DefaultNomenclatureDao
) : CoroutineWorker(
    appContext,
    workerParams
) {
    private val dataSyncManager = DataSyncManager.getInstance(applicationContext)
    private val workManager = WorkManager.getInstance(applicationContext)

    override suspend fun doWork(): Result {
        val startTime = Date()

        // not connected: abort
        if (authManager.getAuthLogin() == null) {
            Log.w(
                TAG,
                "not connected: abort"
            )

            setForeground(
                createForegroundInfo(
                    createNotification(
                        DataSyncStatus(
                            WorkInfo.State.FAILED,
                            applicationContext.getString(R.string.sync_error_server_not_connected),
                            ServerStatus.UNAUTHORIZED
                        )
                    )
                )
            )

            return Result.failure(
                workData(
                    applicationContext.getString(R.string.sync_error_server_not_connected),
                    ServerStatus.UNAUTHORIZED
                )
            )
        }

        val alreadyRunning = workManager
            .getWorkInfosByTag(DATA_SYNC_WORKER_TAG)
            .await()
            .any { it.id != id && it.state == WorkInfo.State.RUNNING }

        if (alreadyRunning) {
            Log.i(
                TAG,
                "already running: abort"
            )

            return Result.retry()
        }

        val checkServerUrlsResult = checkServerUrls(geoNatureAPIClient)

        if (checkServerUrlsResult is Result.Failure) {
            Log.i(
                TAG,
                "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms"
            )

            return checkServerUrlsResult
        }

        setProgress(workData(applicationContext.getString(R.string.sync_start_synchronization)))
        setForeground(createForegroundInfo(createNotification(applicationContext.getString(R.string.sync_start_synchronization))))

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

        val syncNomenclatureResult = syncNomenclature(geoNatureAPIClient)

        if (syncNomenclatureResult is Result.Failure) {
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
                DataSyncSettings.Builder.DEFAULT_PAGE_SIZE
            ),
            inputData.getBoolean(
                INPUT_WITH_ADDITIONAL_DATA,
                true
            )
        )

        Log.i(
            TAG,
            "local data synchronization ${if (syncTaxaResult is Result.Success) "successfully finished" else "finished with failed tasks"} in ${Date().time - startTime.time}ms"
        )

        if (syncTaxaResult is Result.Success) {
            NotificationManagerCompat
                .from(applicationContext)
                .cancel(SYNC_NOTIFICATION_ID)

            dataSyncManager.updateLastSynchronizedDate(
                inputData.getBoolean(
                    INPUT_WITH_ADDITIONAL_DATA,
                    true
                )
            )

            return Result.success(workData(applicationContext.getString(R.string.sync_data_succeeded)))
        }

        return syncTaxaResult
    }

    private fun checkServerUrls(geoNatureAPIClient: IGeoNatureAPIClient): Result {
        return runCatching { geoNatureAPIClient.getBaseUrls() }.fold(
            onSuccess = {
                Log.i(
                    TAG,
                    "starting local data synchronization from '${it.geoNatureBaseUrl}' (with additional data: ${
                        inputData.getBoolean(
                            INPUT_WITH_ADDITIONAL_DATA,
                            true
                        )
                    })..."
                )

                Result.success()
            },
            onFailure = {
                Result.failure(
                    workData(
                        applicationContext.getString(R.string.sync_error_server_url_configuration),
                        ServerStatus.INTERNAL_SERVER_ERROR
                    )
                )
            },
        )
    }

    private suspend fun syncDataset(geoNatureAPIClient: IGeoNatureAPIClient): Result {
        Log.i(
            TAG,
            "synchronize dataset..."
        )

        val result = runCatching {
            geoNatureAPIClient
                .getMetaDatasets()
                .awaitResponse()
        }
            .map {
                checkResponse(it).run {
                    if (this.state == WorkInfo.State.FAILED) this else it
                        .body()
                        ?.byteStream()
                        ?: DataSyncStatus(
                            WorkInfo.State.FAILED,
                            applicationContext.getString(R.string.sync_data_dataset_error)
                        )
                }
            }
            .getOrElse {
                DataSyncStatus(
                    WorkInfo.State.FAILED,
                    applicationContext.getString(R.string.sync_data_dataset_error)
                )
            }

        if (result is DataSyncStatus && result.state == WorkInfo.State.FAILED) {
            setForeground(createForegroundInfo(createNotification(result)))

            return Result.failure(
                workData(
                    result.syncMessage,
                    result.serverStatus
                )
            )
        }

        val dataset = runCatching {
            DatasetJsonReader().read(
                (result as InputStream)
                    .bufferedReader()
                    .use(BufferedReader::readText)
            )
        }.getOrElse { emptyList() }

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
        setForeground(
            createForegroundInfo(
                createNotification(
                    applicationContext.getString(
                        R.string.sync_data_dataset,
                        dataset.size
                    )
                )
            )
        )

        datasetDao.run {
            deleteAll()
            insert(*dataset.toTypedArray())
        }

        return Result.success()
    }

    private suspend fun syncInputObservers(
        geoNatureAPIClient: IGeoNatureAPIClient,
        menuId: Int
    ): Result {
        Log.i(
            TAG,
            "synchronize users..."
        )

        val result = runCatching {
            geoNatureAPIClient
                .getUsers(menuId)
                .awaitResponse()
        }
            .map {
                checkResponse(it).run {
                    if (this.state == WorkInfo.State.FAILED) this else runCatching { it.body() }.getOrNull()
                        ?: emptyList<List<User>>()
                }
            }
            .getOrElse {
                DataSyncStatus(
                    WorkInfo.State.FAILED,
                    applicationContext.getString(R.string.sync_data_observers_error)
                )
            }

        if (result is DataSyncStatus && result.state == WorkInfo.State.FAILED) {
            setForeground(createForegroundInfo(createNotification(result)))

            return Result.failure(
                workData(
                    result.syncMessage,
                    result.serverStatus
                )
            )
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
        setForeground(
            createForegroundInfo(
                createNotification(
                    applicationContext.getString(
                        R.string.sync_data_observers,
                        inputObservers.size
                    )
                )
            )
        )

        inputObserverDao.run {
            deleteAll()
            insert(*inputObservers)
        }

        return Result.success()
    }

    private suspend fun syncTaxonomyRanks(geoNatureAPIClient: IGeoNatureAPIClient): Result {
        Log.i(
            TAG,
            "synchronize taxonomy ranks..."
        )

        val result = runCatching {
            geoNatureAPIClient
                .getTaxonomyRanks()
                .awaitResponse()
        }
            .map {
                checkResponse(it).run {
                    if (this.state == WorkInfo.State.FAILED) this else it
                        .body()
                        ?.byteStream()
                        ?: DataSyncStatus(
                            WorkInfo.State.FAILED,
                            applicationContext.getString(R.string.sync_data_taxonomy_ranks_error)
                        )
                }
            }
            .getOrElse {
                DataSyncStatus(
                    WorkInfo.State.FAILED,
                    applicationContext.getString(R.string.sync_data_taxonomy_ranks_error)
                )
            }

        if (result is DataSyncStatus && result.state == WorkInfo.State.FAILED) {
            setForeground(createForegroundInfo(createNotification(result)))

            return Result.failure(
                workData(
                    result.syncMessage,
                    result.serverStatus
                )
            )
        }

        val taxonomyRanks = runCatching {
            TaxonomyJsonReader().read(
                (result as InputStream)
                    .bufferedReader()
                    .use(BufferedReader::readText)
            )
        }.getOrElse { emptyList() }

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
        setForeground(
            createForegroundInfo(
                createNotification(
                    applicationContext.getString(
                        R.string.sync_data_taxonomy_ranks,
                        taxonomyRanks.size
                    )
                )
            )
        )

        taxonomyDao.run {
            deleteAll()
            insert(*taxonomyRanks.toTypedArray())
        }

        return Result.success()
    }

    private suspend fun syncNomenclature(geoNatureAPIClient: IGeoNatureAPIClient): Result {
        Log.i(
            TAG,
            "synchronize nomenclature types..."
        )

        val nomenclaturesResult = runCatching {
            geoNatureAPIClient
                .getNomenclatures()
                .awaitResponse()
        }
            .map {
                checkResponse(it).run {
                    if (this.state == WorkInfo.State.FAILED) this else runCatching { it.body() }.getOrNull()
                        ?: emptyList<fr.geonature.datasync.api.model.NomenclatureType>()
                }
            }
            .getOrElse {
                DataSyncStatus(
                    WorkInfo.State.FAILED,
                    applicationContext.getString(R.string.sync_data_nomenclature_type_error)
                )
            }

        if (nomenclaturesResult is DataSyncStatus && nomenclaturesResult.state == WorkInfo.State.FAILED) {
            setForeground(createForegroundInfo(createNotification(nomenclaturesResult)))

            return Result.failure(
                workData(
                    nomenclaturesResult.syncMessage,
                    nomenclaturesResult.serverStatus
                )
            )
        }

        val validNomenclatureTypesToUpdate = (nomenclaturesResult as List<*>)
            .asSequence()
            .filterNotNull()
            .map { it as fr.geonature.datasync.api.model.NomenclatureType }
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
        setForeground(
            createForegroundInfo(
                createNotification(
                    applicationContext.getString(
                        R.string.sync_data_nomenclature_type,
                        nomenclatureTypesToUpdate.size
                    )
                )
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
                    fr.geonature.datasync.api.model.NomenclatureTaxonomy(
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
        setForeground(
            createForegroundInfo(
                createNotification(
                    applicationContext.getString(
                        R.string.sync_data_nomenclature,
                        nomenclaturesToUpdate.size
                    )
                )
            )
        )

        Log.i(
            TAG,
            "synchronize nomenclature default values..."
        )

        // TODO: fetch available GeoNature modules
        val defaultNomenclatureResult = runCatching {
            geoNatureAPIClient
                .getDefaultNomenclaturesValues("occtax")
                .awaitResponse()
        }
            .map {
                checkResponse(it).run {
                    if (this.state == WorkInfo.State.FAILED) this else it
                        .body()
                        ?.byteStream()
                        ?: DataSyncStatus(
                            WorkInfo.State.FAILED,
                            applicationContext.getString(R.string.sync_data_nomenclature_default_error)
                        )
                }
            }
            .getOrElse {
                DataSyncStatus(
                    WorkInfo.State.FAILED,
                    applicationContext.getString(R.string.sync_data_nomenclature_default_error)
                )
            }

        if (defaultNomenclatureResult is DataSyncStatus && defaultNomenclatureResult.state == WorkInfo.State.FAILED) {
            setForeground(createForegroundInfo(createNotification(defaultNomenclatureResult)))

            return Result.failure(
                workData(
                    defaultNomenclatureResult.syncMessage,
                    defaultNomenclatureResult.serverStatus
                )
            )
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
        setForeground(
            createForegroundInfo(
                createNotification(
                    applicationContext.getString(
                        R.string.sync_data_nomenclature_default,
                        defaultNomenclaturesToUpdate.size
                    )
                )
            )
        )

        nomenclatureTypeDao.run {
            deleteAll()
            insert(*nomenclatureTypesToUpdate)
        }
        nomenclatureDao.run {
            if (nomenclaturesToUpdate.isNotEmpty()) {
                deleteAll()
                insert(*nomenclaturesToUpdate)
            }
        }
        taxonomyDao.insertOrIgnore(*taxonomyToUpdate)
        nomenclatureTaxonomyDao.insert(*nomenclaturesTaxonomyToUpdate)
        defaultNomenclatureDao.insert(*defaultNomenclaturesToUpdate)

        return Result.success()
    }

    private suspend fun syncTaxa(
        geoNatureAPIClient: IGeoNatureAPIClient,
        listId: Int,
        codeAreaType: String?,
        pageSize: Int,
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
                geoNatureAPIClient
                    .getTaxref(
                        listId,
                        pageSize,
                        offset
                    )
                    .awaitResponse()
            }.getOrNull()


            if (taxrefResponse == null || checkResponse(taxrefResponse).state == WorkInfo.State.FAILED) {
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
                            ?.let { "${it.uppercase(Locale.ROOT)} - ${taxRef.id}" })
                }
                .onEach {
                    validTaxaIds.add(it.id)
                }
                .toList()
                .toTypedArray()

            taxonDao.insert(*taxa)

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
            setForeground(
                createForegroundInfo(
                    createNotification(
                        applicationContext.getString(
                            R.string.sync_data_taxa,
                            (offset + taxa.size)
                        )
                    )
                )
            )

            offset += pageSize
            hasNext = taxref.size == pageSize
        } while (hasNext)

        // delete orphaned taxa
        taxonDao
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
                                taxonDao.deleteById(id)
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
                    geoNatureAPIClient
                        .getTaxrefAreas(
                            codeAreaType,
                            pageSize,
                            offset
                        )
                        .awaitResponse()
                }.getOrNull()

                if (taxrefAreasResponse == null || checkResponse(taxrefAreasResponse).state == WorkInfo.State.FAILED) {
                    hasNext = false
                    continue
                }

                val taxrefAreas =
                    runCatching { taxrefAreasResponse.body() }.getOrDefault(emptyList())

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
                setForeground(
                    createForegroundInfo(
                        createNotification(
                            applicationContext.getString(
                                R.string.sync_data_taxa_areas,
                                (offset + taxrefAreas.size)
                            )
                        )
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


                taxonAreaDao.insert(*taxonAreas)

                Log.i(
                    TAG,
                    "updating ${taxonAreas.size} taxa with areas from offset $offset"
                )

                offset += pageSize
                hasNext = taxrefAreas.size == pageSize
            } while (hasNext)
        }

        return Result.success()
    }

    private fun createForegroundInfo(notification: Notification): ForegroundInfo {
        return ForegroundInfo(
            SYNC_NOTIFICATION_ID,
            notification
        )
    }

    private fun createNotification(
        contentText: CharSequence?,
        componentClassIntent: Class<*> = HomeActivity::class.java
    ): Notification {
        return NotificationCompat
            .Builder(
                applicationContext,
                MainApplication.CHANNEL_DATA_SYNCHRONIZATION
            )
            .setAutoCancel(true)
            .setContentTitle(applicationContext.getText(R.string.notification_data_synchronization_title))
            .setContentText(contentText)
            .setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    Intent(
                        applicationContext,
                        componentClassIntent
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    },
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                )
            )
            .setSmallIcon(R.drawable.ic_sync)
            .build()
    }

    private fun createNotification(dataSyncStatus: DataSyncStatus): Notification {
        return createNotification(
            if (dataSyncStatus.serverStatus == ServerStatus.UNAUTHORIZED) applicationContext.getString(R.string.sync_error_server_not_connected)
            else dataSyncStatus.syncMessage,
            if (dataSyncStatus.serverStatus == ServerStatus.UNAUTHORIZED) LoginActivity::class.java
            else HomeActivity::class.java
        )
    }

    private suspend fun checkResponse(response: Response<*>?): DataSyncStatus {
        // not connected
        if (response?.code() == ServerStatus.UNAUTHORIZED.httpStatus) {
            setForeground(
                createForegroundInfo(
                    createNotification(
                        applicationContext.getString(R.string.sync_error_server_not_connected),
                        LoginActivity::class.java
                    )
                )
            )

            return DataSyncStatus(
                WorkInfo.State.FAILED,
                applicationContext.getString(R.string.sync_error_server_not_connected),
                ServerStatus.UNAUTHORIZED
            )
        }

        if (response?.isSuccessful == false) {
            return DataSyncStatus(
                WorkInfo.State.FAILED,
                applicationContext.getString(R.string.sync_error_server_error),
                ServerStatus.INTERNAL_SERVER_ERROR
            )
        }

        return DataSyncStatus(
            WorkInfo.State.RUNNING,
            null,
            ServerStatus.OK
        )
    }

    private fun workData(
        syncMessage: String?,
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

        const val SYNC_NOTIFICATION_ID = 3

        private const val INPUT_USERS_MENU_ID = "usersMenuId"
        private const val INPUT_TAXREF_LIST_ID = "taxrefListId"
        private const val INPUT_CODE_AREA_TYPE = "codeAreaType"
        private const val INPUT_PAGE_SIZE = "pageSize"
        private const val INPUT_WITH_ADDITIONAL_DATA = "withAdditionalData"

        /**
         * Configure input data to this [DataSyncWorker] from given [DataSyncSettings].
         */
        fun inputData(
            dataSyncSettings: DataSyncSettings,
            withAdditionalData: Boolean = true
        ): Data {
            return Data
                .Builder()
                .putAll(
                    mapOf(
                        Pair(
                            INPUT_USERS_MENU_ID,
                            dataSyncSettings.usersListId
                        ),
                        Pair(
                            INPUT_TAXREF_LIST_ID,
                            dataSyncSettings.taxrefListId
                        ),
                        Pair(
                            INPUT_CODE_AREA_TYPE,
                            dataSyncSettings.codeAreaType
                        ),
                        Pair(
                            INPUT_PAGE_SIZE,
                            dataSyncSettings.pageSize
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
