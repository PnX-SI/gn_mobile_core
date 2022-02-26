package fr.geonature.datasync.sync.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager.getInstance
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
import fr.geonature.datasync.R
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.api.model.User
import fr.geonature.datasync.auth.IAuthManager
import fr.geonature.datasync.auth.worker.CheckAuthLoginWorker
import fr.geonature.datasync.packageinfo.worker.CheckInputsToSynchronizeWorker
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.sync.DataSyncStatus
import fr.geonature.datasync.sync.IDataSyncManager
import fr.geonature.datasync.sync.ServerStatus
import fr.geonature.datasync.sync.io.DatasetJsonReader
import fr.geonature.datasync.sync.io.TaxonomyJsonReader
import fr.geonature.datasync.ui.login.LoginActivity
import org.json.JSONObject
import org.tinylog.Logger
import retrofit2.Response
import retrofit2.awaitResponse
import java.io.BufferedReader
import java.io.InputStream
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

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
    private val dataSyncManager: IDataSyncManager,
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
    private val workManager = getInstance(applicationContext)

    override suspend fun doWork(): Result {
        val startTime = Date()

        // not connected: abort
        if (authManager.getAuthLogin() == null) {
            Logger.warn { "not connected: abort" }

            sendNotification(
                DataSyncStatus(
                    WorkInfo.State.FAILED,
                    applicationContext.getString(R.string.sync_error_server_not_connected),
                    ServerStatus.UNAUTHORIZED
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
            Logger.warn { "already running: abort" }

            return Result.retry()
        }

        val checkServerUrlsResult = checkServerUrls(geoNatureAPIClient)

        if (checkServerUrlsResult is Result.Failure) {
            Logger.info { "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms" }

            return checkServerUrlsResult
        }

        setProgress(workData(applicationContext.getString(R.string.sync_start_synchronization)))
        sendNotification(applicationContext.getString(R.string.sync_start_synchronization))

        val syncDatasetResult = syncDataset(geoNatureAPIClient)

        if (syncDatasetResult is Result.Failure) {
            Logger.info { "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms" }

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
            Logger.info { "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms" }

            return syncInputObserversResult
        }

        val syncTaxonomyRanksResult = syncTaxonomyRanks(geoNatureAPIClient)

        if (syncTaxonomyRanksResult is Result.Failure) {
            Logger.info { "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms" }

            return syncTaxonomyRanksResult
        }

        val syncNomenclatureResult = syncNomenclature(geoNatureAPIClient)

        if (syncNomenclatureResult is Result.Failure) {
            Logger.info { "local data synchronization finished with failed tasks in ${Date().time - startTime.time}ms" }

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

        Logger.info { "local data synchronization ${if (syncTaxaResult is Result.Success) "successfully finished" else "finished with failed tasks"} in ${Date().time - startTime.time}ms" }

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
                Logger.info {
                    "starting local data synchronization from '${it.geoNatureBaseUrl}' (with additional data: ${
                        inputData.getBoolean(
                            INPUT_WITH_ADDITIONAL_DATA,
                            true
                        )
                    })..."
                }

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
        Logger.info { "synchronize dataset..." }

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
            sendNotification(result)

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

        Logger.info { "dataset to update: ${dataset.size}" }

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
        Logger.info { "synchronize users..." }

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
            sendNotification(result)

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

        Logger.info { "users to update: ${inputObservers.size}" }

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

        inputObserverDao.run {
            deleteAll()
            insert(*inputObservers)
        }

        return Result.success()
    }

    private suspend fun syncTaxonomyRanks(geoNatureAPIClient: IGeoNatureAPIClient): Result {
        Logger.info { "synchronize taxonomy ranks..." }

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
            sendNotification(result)

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

        Logger.info { "taxonomy ranks to update: ${taxonomyRanks.size}" }

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

        taxonomyDao.run {
            deleteAll()
            insert(*taxonomyRanks.toTypedArray())
        }

        return Result.success()
    }

    private suspend fun syncNomenclature(geoNatureAPIClient: IGeoNatureAPIClient): Result {
        Logger.info { "synchronize nomenclature types..." }

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
            sendNotification(nomenclaturesResult)

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

        Logger.info { "nomenclature types to update: ${nomenclatureTypesToUpdate.size}" }

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

        Logger.info { "nomenclature to update: ${nomenclaturesToUpdate.size}" }

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

        Logger.info { "synchronize nomenclature default values..." }

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
            sendNotification(defaultNomenclatureResult)

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

        Logger.info { "nomenclature default values to update: ${defaultNomenclaturesToUpdate.size}" }

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
        Logger.info { "synchronize taxa..." }

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

            Logger.info { "taxa to update: ${offset + taxa.size}" }

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

            offset += pageSize
            hasNext = taxref.size == pageSize
        } while (hasNext)

        // delete orphaned taxa
        taxonDao
            .QB()
            .cursor()
            .run {
                Logger.info { "deleting orphaned taxa..." }

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

                Logger.info { "orphaned taxa deleted: ${orphanedTaxaIds.size}" }
            }

        if (withAdditionalData) {
            Logger.info { "synchronize taxa additional data..." }

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

                Logger.info { "found ${taxrefAreas.size} taxa with areas from offset $offset" }

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


                taxonAreaDao.insert(*taxonAreas)

                Logger.info { "updating ${taxonAreas.size} taxa with areas from offset $offset" }

                offset += pageSize
                hasNext = taxrefAreas.size == pageSize
            } while (hasNext)
        }

        return Result.success()
    }

    private suspend fun sendNotification(
        dataSyncStatus: DataSyncStatus,
        componentClassIntent: Class<*>? = null
    ) {
        sendNotification(
            if (dataSyncStatus.serverStatus == ServerStatus.UNAUTHORIZED) applicationContext.getString(R.string.sync_error_server_not_connected)
            else dataSyncStatus.syncMessage,
            componentClassIntent
        )
    }

    private suspend fun sendNotification(
        contentText: CharSequence?,
        componentClassIntent: Class<*>? = null
    ) {
        val notificationChannelId = inputData.getString(INPUT_NOTIFICATION_CHANNEL_ID)
        val intentClassName = inputData.getString(INPUT_INTENT_CLASS_NAME)

        if (notificationChannelId.isNullOrBlank() || intentClassName.isNullOrBlank()) {
            return
        }

        val componentClassIntentOrDefault =
            runCatching { Class.forName(intentClassName) }.getOrElse { componentClassIntent }

        if (componentClassIntentOrDefault == null) {
            Logger.warn { "no notification will be sent as intent class name '$intentClassName' was not found" }

            return
        }

        setForeground(
            ForegroundInfo(
                SYNC_NOTIFICATION_ID,
                NotificationCompat
                    .Builder(
                        applicationContext,
                        notificationChannelId
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
                                componentClassIntentOrDefault
                            ).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            },
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                        )
                    )
                    .setSmallIcon(R.drawable.ic_sync)
                    .build()
            )
        )
    }

    private suspend fun checkResponse(response: Response<*>?): DataSyncStatus {
        // not connected
        if (response?.code() == ServerStatus.UNAUTHORIZED.httpStatus) {
            sendNotification(
                applicationContext.getString(R.string.sync_error_server_not_connected),
                LoginActivity::class.java
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

        const val KEY_SYNC_MESSAGE = "KEY_SYNC_MESSAGE"
        const val KEY_SERVER_STATUS = "KEY_SERVER_STATUS"

        // the name of the synchronization work
        private const val DATA_SYNC_WORKER = "data_sync_worker"
        const val DATA_SYNC_WORKER_PERIODIC = "data_sync_worker_periodic"
        const val DATA_SYNC_WORKER_PERIODIC_ESSENTIAL = "data_sync_worker_periodic_essential"
        const val DATA_SYNC_WORKER_TAG = "data_sync_worker_tag"

        const val SYNC_NOTIFICATION_ID = 3

        private const val INPUT_USERS_MENU_ID = "usersMenuId"
        private const val INPUT_TAXREF_LIST_ID = "taxrefListId"
        private const val INPUT_CODE_AREA_TYPE = "codeAreaType"
        private const val INPUT_PAGE_SIZE = "pageSize"
        private const val INPUT_WITH_ADDITIONAL_DATA = "withAdditionalData"
        private const val INPUT_INTENT_CLASS_NAME = "intent_class_name"
        private const val INPUT_NOTIFICATION_CHANNEL_ID = "notification_channel_id"

        /**
         * Convenience method for enqueuing unique work to this worker.
         */
        fun enqueueUniqueWork(
            context: Context,
            dataSyncSettings: DataSyncSettings,
            notificationComponentClassIntent: Class<*>,
            notificationChannelId: String
        ): UUID {
            val dataSyncWorkRequest = OneTimeWorkRequest
                .Builder(DataSyncWorker::class.java)
                .addTag(DATA_SYNC_WORKER_TAG)
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    inputData(
                        dataSyncSettings,
                        true,
                        notificationComponentClassIntent,
                        notificationChannelId
                    )
                )
                .build()

            return dataSyncWorkRequest.id.also {
                getInstance(context).enqueueUniqueWork(
                    DATA_SYNC_WORKER,
                    ExistingWorkPolicy.REPLACE,
                    dataSyncWorkRequest
                )
            }
        }

        /**
         * Convenience method for enqueuing periodic work to this worker.
         */
        fun enqueueUniquePeriodicWork(
            context: Context,
            dataSyncSettings: DataSyncSettings,
            withAdditionalData: Boolean = true,
            notificationComponentClassIntent: Class<*>,
            notificationChannelId: String = CheckAuthLoginWorker.DEFAULT_CHANNEL_DATA_SYNCHRONIZATION,
            repeatInterval: Duration = 15.toDuration(DurationUnit.MINUTES)
        ) {
            getInstance(context).enqueueUniquePeriodicWork(
                if (withAdditionalData) DATA_SYNC_WORKER_PERIODIC else DATA_SYNC_WORKER_PERIODIC_ESSENTIAL,
                ExistingPeriodicWorkPolicy.REPLACE,
                PeriodicWorkRequestBuilder<CheckInputsToSynchronizeWorker>(repeatInterval.toJavaDuration())
                    .addTag(DATA_SYNC_WORKER_TAG)
                    .setConstraints(
                        Constraints
                            .Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .setInitialDelay(
                        (if (withAdditionalData) 15.toDuration(DurationUnit.MINUTES) else (repeatInterval / 2).coerceAtLeast(30.toDuration(DurationUnit.MINUTES))).inWholeSeconds,
                        TimeUnit.SECONDS
                    )
                    .setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        (if (withAdditionalData) 1 else 2).toDuration(DurationUnit.MINUTES).inWholeSeconds,
                        TimeUnit.SECONDS
                    )
                    .setInputData(
                        inputData(
                            dataSyncSettings,
                            withAdditionalData,
                            notificationComponentClassIntent,
                            notificationChannelId
                        )
                    )
                    .build()
            )
        }

        /**
         * Configure input data to this worker from given [DataSyncSettings].
         */
        private fun inputData(
            dataSyncSettings: DataSyncSettings,
            withAdditionalData: Boolean = true,
            notificationComponentClassIntent: Class<*>,
            notificationChannelId: String
        ): Data {
            return Data
                .Builder()
                .putInt(
                    INPUT_USERS_MENU_ID,
                    dataSyncSettings.usersListId
                )
                .putInt(
                    INPUT_TAXREF_LIST_ID,
                    dataSyncSettings.taxrefListId
                )
                .putString(
                    INPUT_CODE_AREA_TYPE,
                    dataSyncSettings.codeAreaType
                )
                .putInt(
                    INPUT_PAGE_SIZE,
                    dataSyncSettings.pageSize
                )
                .putBoolean(
                    INPUT_WITH_ADDITIONAL_DATA,
                    withAdditionalData
                )
                .putString(
                    INPUT_INTENT_CLASS_NAME,
                    notificationComponentClassIntent.name
                )
                .putString(
                    INPUT_NOTIFICATION_CHANNEL_ID,
                    notificationChannelId
                )
                .build()
        }
    }
}
