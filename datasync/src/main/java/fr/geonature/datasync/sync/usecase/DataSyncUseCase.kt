package fr.geonature.datasync.sync.usecase

import android.app.Application
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.core.content.edit
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.room.withTransaction
import androidx.work.WorkInfo
import fr.geonature.commons.data.GeoNatureModuleName
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.entity.DefaultNomenclature
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureTaxonomy
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.TaxonList
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.interactor.BaseFlowUseCase
import fr.geonature.commons.util.toIsoDateString
import fr.geonature.datasync.R
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.api.error.BaseApiException
import fr.geonature.datasync.api.model.DatasetQuery
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.sync.DataSyncStatus
import fr.geonature.datasync.sync.ServerStatus
import fr.geonature.datasync.sync.SynchronizeAdditionalFieldsRepository
import fr.geonature.datasync.sync.io.DatasetJsonReader
import fr.geonature.datasync.sync.io.TaxonomyJsonReader
import fr.geonature.datasync.sync.repository.ISynchronizeLocalDataRepository
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import org.tinylog.Logger
import retrofit2.await
import java.io.BufferedReader
import java.util.Date
import javax.inject.Inject

/**
 * Synchronize all local data from _GeoNature_ APIs.
 *
 * @author S. Grimault
 */
class DataSyncUseCase @Inject constructor(
    private val application: Application,
    @GeoNatureModuleName private val moduleName: String,
    private val geoNatureAPIClient: IGeoNatureAPIClient,
    private val database: LocalDatabase,
    @SynchronizeAdditionalFieldsRepository private val synchronizeAdditionalFieldsRepository: ISynchronizeLocalDataRepository
) : BaseFlowUseCase<DataSyncStatus, DataSyncUseCase.Params>() {

    private val sharedPreferences: SharedPreferences = getDefaultSharedPreferences(application)

    override suspend fun run(params: Params): Flow<DataSyncStatus> =
        channelFlow {
            database.withTransaction {
                checkServerUrls(params.withAdditionalData).collect {
                    sendOrThrow(
                        this,
                        it
                    )
                }
                synchronizeDataset().collect {
                    sendOrThrow(
                        this,
                        it
                    )
                }
                synchronizeObservers(params.usersMenuId).collect {
                    sendOrThrow(
                        this,
                        it
                    )
                }
                synchronizeTaxonomyRanks().collect {
                    sendOrThrow(
                        this,
                        it
                    )
                }
                synchronizeNomenclature().collect {
                    sendOrThrow(
                        this,
                        it
                    )
                }
                synchronizeTaxa(
                    params.codeAreaType,
                    params.pageSize,
                    params.withAdditionalData
                ).collect {
                    sendOrThrow(
                        this,
                        it
                    )
                }

                if (params.withAdditionalFields) {
                    synchronizeAdditionalFieldsRepository().collect {
                        sendOrThrow(
                            this,
                            it
                        )
                    }
                }
            }
        }

    private fun checkServerUrls(withAdditionalData: Boolean = true): Flow<DataSyncStatus> =
        flow {
            runCatching { geoNatureAPIClient.getBaseUrls() }.fold(
                onSuccess = {
                    if (geoNatureAPIClient.checkSettings()) {
                        Logger.info {
                            "starting local data synchronization from '${it.geoNatureBaseUrl}' (with additional data: $withAdditionalData)..."
                        }

                        emit(DataSyncStatus(state = WorkInfo.State.SUCCEEDED))
                    } else {
                        emit(
                            DataSyncStatus(
                                state = WorkInfo.State.FAILED,
                                syncMessage = application.getString(R.string.sync_error_server_url_configuration),
                                serverStatus = ServerStatus.INTERNAL_SERVER_ERROR
                            )
                        )
                    }
                },
                onFailure = {
                    emit(
                        DataSyncStatus(
                            state = WorkInfo.State.FAILED,
                            syncMessage = application.getString(R.string.sync_error_server_url_configuration),
                            serverStatus = ServerStatus.INTERNAL_SERVER_ERROR
                        )
                    )
                },
            )
        }

    private suspend fun synchronizeDataset(): Flow<DataSyncStatus> =
        flow {
            Logger.info { "synchronize dataset..." }

            val response = runCatching {
                geoNatureAPIClient
                    .getMetaDatasets(DatasetQuery(code = moduleName.uppercase()))
                    .await()
            }
                .onFailure {
                    emit(
                        onFailure(
                            it,
                            application.getString(R.string.sync_data_dataset_error)
                        )
                    )
                }
                .getOrNull()
                ?: return@flow

            val dataset = runCatching {
                DatasetJsonReader().read(
                    response
                        .byteStream()
                        .bufferedReader()
                        .use(BufferedReader::readText)
                )
            }.getOrElse { emptyList() }

            Logger.info { "dataset to update: ${dataset.size}" }

            if (dataset.isEmpty()) {
                emit(DataSyncStatus(state = WorkInfo.State.SUCCEEDED))

                return@flow
            }

            runCatching {
                database
                    .datasetDao()
                    .run {
                        deleteAll()
                        insert(*dataset.toTypedArray())
                    }
            }.onFailure {
                emit(
                    onFailure(
                        it,
                        application.getString(R.string.sync_data_dataset_error)
                    )
                )
            }

            emit(
                DataSyncStatus(
                    state = WorkInfo.State.SUCCEEDED,
                    syncMessage = application.getString(
                        R.string.sync_data_dataset,
                        dataset.size
                    )
                )
            )
        }

    private suspend fun synchronizeObservers(usersMenuId: Int = 0): Flow<DataSyncStatus> =
        flow {
            Logger.info { "synchronize users..." }

            val response = runCatching {
                geoNatureAPIClient
                    .getUsers(usersMenuId)
                    .await()
            }
                .onFailure {
                    emit(
                        onFailure(
                            it,
                            application.getString(R.string.sync_data_observers_error)
                        )
                    )
                }
                .getOrNull()
                ?: return@flow

            val inputObservers = response
                .asSequence()
                .map {
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
                emit(DataSyncStatus(state = WorkInfo.State.SUCCEEDED))

                return@flow
            }

            runCatching {
                database
                    .inputObserverDao()
                    .run {
                        deleteAll()
                        insert(*inputObservers)
                    }
            }.onFailure {
                emit(
                    onFailure(
                        it,
                        application.getString(R.string.sync_data_observers_error)
                    )
                )
            }

            emit(
                DataSyncStatus(
                    state = WorkInfo.State.SUCCEEDED,
                    syncMessage = application.getString(
                        R.string.sync_data_observers,
                        inputObservers.size
                    )
                )
            )
        }

    private suspend fun synchronizeTaxonomyRanks(): Flow<DataSyncStatus> =
        flow {
            Logger.info { "synchronize taxonomy ranks..." }

            val response = runCatching {
                geoNatureAPIClient
                    .getTaxonomyRanks()
                    .await()
            }
                .onFailure {
                    emit(
                        onFailure(
                            it,
                            application.getString(R.string.sync_data_taxonomy_ranks_error)
                        )
                    )
                }
                .getOrNull()
                ?: return@flow

            val taxonomyRanks = runCatching {
                TaxonomyJsonReader().read(
                    response
                        .byteStream()
                        .bufferedReader()
                        .use(BufferedReader::readText)
                )
            }.getOrElse { emptyList() }

            Logger.info { "taxonomy ranks to update: ${taxonomyRanks.size}" }

            if (taxonomyRanks.isEmpty()) {
                emit(DataSyncStatus(state = WorkInfo.State.SUCCEEDED))

                return@flow
            }

            runCatching {
                database
                    .taxonomyDao()
                    .run {
                        deleteAll()
                        insert(*taxonomyRanks.toTypedArray())
                    }
            }.onFailure {
                emit(
                    onFailure(
                        it,
                        application.getString(R.string.sync_data_taxonomy_ranks_error)
                    )
                )
            }

            emit(
                DataSyncStatus(
                    state = WorkInfo.State.SUCCEEDED,
                    syncMessage = application.getString(
                        R.string.sync_data_taxonomy_ranks,
                        taxonomyRanks.size
                    )
                )
            )
        }

    private suspend fun synchronizeNomenclature(): Flow<DataSyncStatus> =
        flow {
            Logger.info { "synchronize nomenclature types..." }

            val nomenclaturesResponse = runCatching {
                geoNatureAPIClient
                    .getNomenclatures()
                    .await()
            }
                .onFailure {
                    emit(
                        onFailure(
                            it,
                            application.getString(R.string.sync_data_nomenclature_type_error)
                        )
                    )
                }
                .getOrNull()
                ?: return@flow

            val validNomenclatureTypesToUpdate = nomenclaturesResponse
                .asSequence()
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
                emit(DataSyncStatus(state = WorkInfo.State.SUCCEEDED))
                return@flow
            }

            emit(
                DataSyncStatus(
                    state = WorkInfo.State.SUCCEEDED,
                    syncMessage = application.getString(
                        R.string.sync_data_nomenclature_type,
                        nomenclatureTypesToUpdate.size
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
                .flatMap { it }
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
                .flatMap { it }
                .toList()
                .toTypedArray()

            Logger.info { "nomenclature to update: ${nomenclaturesToUpdate.size}" }

            val taxonomyToUpdate = nomenclaturesTaxonomyToUpdate
                .asSequence()
                .map { it.taxonomy }
                .toList()
                .toTypedArray()

            emit(
                DataSyncStatus(
                    state = WorkInfo.State.SUCCEEDED,
                    syncMessage = application.getString(
                        R.string.sync_data_nomenclature,
                        nomenclaturesToUpdate.size
                    )
                )
            )

            Logger.info { "synchronize nomenclature default values..." }

            val defaultNomenclatureResponse = runCatching {
                geoNatureAPIClient
                    .getDefaultNomenclaturesValues(moduleName)
                    .await()
            }
                .onFailure {
                    emit(
                        onFailure(
                            it,
                            application.getString(R.string.sync_data_nomenclature_default_error)
                        )
                    )
                }
                .getOrNull()
                ?: return@flow

            val defaultNomenclatureAsJson = runCatching {
                JSONObject(
                    defaultNomenclatureResponse
                        .byteStream()
                        .bufferedReader()
                        .use(BufferedReader::readText)
                )
            }.getOrNull()
                ?: JSONObject()

            val defaultNomenclaturesToUpdate = defaultNomenclatureAsJson
                .keys()
                .asSequence()
                .filter { mnemonic ->
                    val nomenclatureType =
                        nomenclatureTypesToUpdate.find { it.mnemonic == mnemonic }
                            ?: return@filter run {
                                Logger.warn { "no such nomenclature type '$mnemonic' with default value" }
                                false
                            }
                    val defaultNomenclatureId = defaultNomenclatureAsJson.getLong(mnemonic)

                    run {
                        val predicate =
                            nomenclaturesToUpdate.any { it.typeId == nomenclatureType.id && it.id == defaultNomenclatureId }

                        if (!predicate) {
                            Logger.warn { "no default nomenclature value found with mnemonic '$mnemonic'" }
                        }

                        predicate
                    }
                }
                .map {
                    DefaultNomenclature(
                        moduleName,
                        defaultNomenclatureAsJson.getLong(it)
                    )
                }
                .toList()
                .toTypedArray()

            Logger.info { "nomenclature default values to update: ${defaultNomenclaturesToUpdate.size}" }

            emit(
                DataSyncStatus(
                    state = WorkInfo.State.SUCCEEDED,
                    syncMessage = application.getString(
                        R.string.sync_data_nomenclature_default,
                        defaultNomenclaturesToUpdate.size
                    )
                )
            )

            runCatching {
                database
                    .nomenclatureTypeDao()
                    .run {
                        deleteAll()
                        insert(*nomenclatureTypesToUpdate)
                    }
            }.onFailure {
                emit(
                    onFailure(
                        it,
                        application.getString(R.string.sync_data_nomenclature_type_error)
                    )
                )
            }

            runCatching {
                database
                    .nomenclatureDao()
                    .run {
                        if (nomenclaturesToUpdate.isNotEmpty()) {
                            deleteAll()
                            insert(*nomenclaturesToUpdate)
                        }
                    }
            }.onFailure {
                emit(
                    onFailure(
                        it,
                        application.getString(R.string.sync_data_nomenclature_error)
                    )
                )
            }

            runCatching {
                database
                    .taxonomyDao()
                    .insertOrIgnore(*taxonomyToUpdate)
            }.onFailure {
                emit(
                    onFailure(
                        it,
                        application.getString(R.string.sync_data_taxonomy_ranks_error)
                    )
                )
            }

            runCatching {
                database
                    .nomenclatureTaxonomyDao()
                    .insert(*nomenclaturesTaxonomyToUpdate)
            }.onFailure {
                emit(
                    onFailure(
                        it,
                        application.getString(R.string.sync_data_nomenclature_error)
                    )
                )
            }

            runCatching {
                database
                    .defaultNomenclatureDao()
                    .insert(*defaultNomenclaturesToUpdate)
            }.onFailure {
                emit(
                    onFailure(
                        it,
                        application.getString(R.string.sync_data_nomenclature_default_error)
                    )
                )
            }
        }

    private suspend fun synchronizeTaxa(
        codeAreaType: String?,
        pageSize: Int,
        withAdditionalData: Boolean = true
    ): Flow<DataSyncStatus> =
        flow {
            val lastUpdatedDate = getTaxaLastUpdatedDate()?.also {
                Logger.info { "taxa last synchronization date: ${it.toIsoDateString()}" }
            }
            val taxaLastUpdatedDate = runCatching {
                geoNatureAPIClient
                    .getTaxrefVersion()
                    .await()
            }
                .onFailure {
                    Logger.warn { "failed to get taxa last updated date from GeoNature..." }
                }
                .getOrNull()?.updatedAt?.also {
                    Logger.info { "taxa last synchronization date from remote: ${it.toIsoDateString()}" }
                }
            val hasLocalData = runCatching {
                !database
                    .taxonDao()
                    .isEmpty()
            }.getOrDefault(false)

            var hasNext: Boolean
            var offset = 0
            var page = 1
            var hasErrors = false

            val validTaxaIds = mutableSetOf<Long>()

            if (!hasLocalData || lastUpdatedDate == null || taxaLastUpdatedDate == null || taxaLastUpdatedDate.after(lastUpdatedDate)) {
                Logger.info { "synchronize taxa..." }

                runCatching {
                    database
                        .taxonDao()
                        .deleteAll()
                }.onFailure { Logger.warn(it) { "failed to deleting existing taxa" } }

                // fetch all taxa from paginated list
                do {
                    val taxrefListResult = runCatching {
                        geoNatureAPIClient
                            .getTaxref(
                                pageSize,
                                page
                            )
                            .await()
                    }
                        .onFailure {
                            Logger.warn { "taxa synchronization finished with errors" }
                            hasErrors = true
                            emit(
                                onFailure(
                                    it,
                                    application.getString(R.string.sync_data_taxa_with_errors)
                                )
                            )
                        }
                        .getOrNull()
                        ?: return@flow

                    if (taxrefListResult.items.isEmpty()) {
                        hasNext = false
                        continue
                    }

                    val taxa = taxrefListResult.items
                        .asSequence()
                        .map { taxRef ->
                            // check if this taxon as a valid taxonomy definition
                            if (taxRef.kingdom.isNullOrBlank() || taxRef.group.isNullOrBlank()) {
                                Logger.warn { "invalid taxon with ID '${taxRef.id}' found: no taxonomy defined" }

                                return@map null
                            }

                            Taxon(
                                id = taxRef.id,
                                name = taxRef.name.trim(),
                                taxonomy = Taxonomy(
                                    taxRef.kingdom,
                                    taxRef.group
                                ),
                                commonName = taxRef.commonName?.trim(),
                                description = taxRef.fullName?.trim()
                            )
                        }
                        .filterNotNull()
                        .onEach {
                            validTaxaIds.add(it.id)
                        }
                        .toList()
                        .toTypedArray()

                    val taxaList = taxrefListResult.items
                        .asSequence()
                        .filter { taxRef -> validTaxaIds.any { it == taxRef.id } }
                        .flatMap { taxRef ->
                            (taxRef.list
                                ?: emptyList()).map {
                                TaxonList(
                                    taxRef.id,
                                    it
                                )
                            }
                        }
                        .toList()
                        .toTypedArray()

                    runCatching {
                        database
                            .taxonDao()
                            .insert(*taxa)
                        database
                            .taxonListDao()
                            .insert(*taxaList)
                    }.onFailure {
                        Logger.warn(it) { "failed to update taxa (page: $page)" }
                        hasErrors = true
                    }

                    Logger.info { "taxa to update: ${offset + taxa.size}" }

                    emit(
                        DataSyncStatus(
                            state = WorkInfo.State.SUCCEEDED,
                            syncMessage = application.getString(
                                R.string.sync_data_taxa,
                                (offset + taxa.size)
                            )
                        )
                    )

                    offset += pageSize
                    page++
                    hasNext = taxrefListResult.items.size == pageSize
                } while (hasNext && !hasErrors)

                updateTaxaLastUpdatedDate()

                delay(1000)
            } else {
                validTaxaIds.addAll(runCatching {
                    database
                        .taxonDao()
                        .findAll()
                        .map { it.id }
                }.getOrDefault(emptyList()))
            }

            if (withAdditionalData && codeAreaType?.isNotBlank() == true) {
                Logger.info { "synchronize taxa additional data..." }

                offset = 0
                page = 1

                // fetch all taxa areas from paginated list
                do {
                    val taxrefAreasResponse = runCatching {
                        geoNatureAPIClient
                            .getTaxrefAreas(
                                codeAreaType,
                                pageSize,
                                page
                            )
                            .await()
                    }
                        .onFailure {
                            Logger.warn { "taxa by area synchronization finished with errors" }
                            emit(
                                onFailure(
                                    it,
                                    application.getString(R.string.sync_data_taxa_areas_with_errors)
                                )
                            )
                        }
                        .getOrNull()
                        ?: return@flow

                    if (taxrefAreasResponse.isEmpty()) {
                        hasNext = false
                        continue
                    }

                    Logger.info { "found ${taxrefAreasResponse.size} taxa with areas from page $page" }

                    emit(
                        DataSyncStatus(
                            state = WorkInfo.State.SUCCEEDED,
                            syncMessage = application.getString(
                                R.string.sync_data_taxa_areas,
                                (offset + taxrefAreasResponse.size)
                            )
                        )
                    )

                    val taxonAreas = taxrefAreasResponse
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

                    runCatching {
                        database
                            .taxonAreaDao()
                            .insert(*taxonAreas)
                    }.onFailure { Logger.warn(it) { "failed to update taxa with areas (page: $page)" } }

                    Logger.info { "updating ${taxonAreas.size} taxa with areas from page $page" }

                    offset += pageSize
                    page++
                    hasNext = taxrefAreasResponse.size == pageSize
                } while (hasNext)
            }
        }

    private fun onFailure(
        throwable: Throwable,
        errorMessage: String? = null
    ): DataSyncStatus {
        return when (throwable) {
            is BaseApiException.UnauthorizedException -> {
                DataSyncStatus(
                    state = WorkInfo.State.FAILED,
                    syncMessage = application.getString(R.string.sync_error_server_not_connected),
                    serverStatus = ServerStatus.UNAUTHORIZED
                )
            }

            is BaseApiException.InternalServerException -> {
                DataSyncStatus(
                    state = WorkInfo.State.FAILED,
                    syncMessage = application.getString(R.string.sync_error_server_error),
                    serverStatus = ServerStatus.INTERNAL_SERVER_ERROR
                )
            }

            else -> {
                DataSyncStatus(
                    state = WorkInfo.State.FAILED,
                    syncMessage = errorMessage
                )
            }
        }
    }

    /**
     * Sends synchronization status to the given channel and throw exception if its current status
     * is [WorkInfo.State.FAILED] to cancel the current transaction.
     */
    private suspend fun sendOrThrow(
        channel: SendChannel<DataSyncStatus>,
        dataSyncStatus: DataSyncStatus
    ) {
        channel.send(dataSyncStatus)

        if (dataSyncStatus.state == WorkInfo.State.FAILED) {
            delay(500)
            throw java.lang.Exception(dataSyncStatus.syncMessage)
        }
    }

    private fun getTaxaLastUpdatedDate(): Date? {
        return this.sharedPreferences
            .getLong(
                KEY_SYNC_TAXA_LAST_UPDATED_AT,
                -1L
            )
            .takeUnless { it == -1L }
            ?.let { Date(it) }
    }

    private fun updateTaxaLastUpdatedDate() {
        this.sharedPreferences.edit {
            putLong(
                KEY_SYNC_TAXA_LAST_UPDATED_AT,
                Date().time
            )
        }
    }

    data class Params(
        val withAdditionalData: Boolean = true,
        val withAdditionalFields: Boolean = false,
        val usersMenuId: Int = 0,
        val codeAreaType: String?,
        val pageSize: Int = DataSyncSettings.Builder.DEFAULT_PAGE_SIZE
    )

    companion object {
        private const val KEY_SYNC_TAXA_LAST_UPDATED_AT = "key_sync_taxa_last_updated_at"
    }
}