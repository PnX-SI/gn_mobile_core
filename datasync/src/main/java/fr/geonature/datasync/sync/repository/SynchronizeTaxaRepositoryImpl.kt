package fr.geonature.datasync.sync.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.WorkInfo
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.TaxonList
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.toIsoDateString
import fr.geonature.datasync.R
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.sync.DataSyncStatus
import fr.geonature.datasync.sync.repository.ISynchronizeLocalDataRepository.Companion.sendOrThrow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import org.tinylog.Logger
import retrofit2.await
import java.util.Date

/**
 * Default repository interface to synchronize taxa and their relative data.
 */
interface ISynchronizeTaxaRepository :
    ISynchronizeLocalDataRepository<ISynchronizeTaxaRepository.Params> {
    data class Params(
        val withAdditionalData: Boolean = true,
        val codeAreaType: String?,
        val pageSize: Int = DataSyncSettings.Builder.DEFAULT_PAGE_SIZE
    )
}

/**
 * Implementation of [ISynchronizeLocalDataRepository] to synchronize taxa and their relative data.
 *
 * @author S. Grimault
 */
class SynchronizeTaxaRepositoryImpl(
    private val context: Context,
    private val geoNatureAPIClient: IGeoNatureAPIClient,
    private val database: LocalDatabase
) : ISynchronizeTaxaRepository {

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun invoke(params: ISynchronizeTaxaRepository.Params): Flow<DataSyncStatus> =
        channelFlow {
            val lastUpdatedDate = getTaxaLastUpdatedDate()?.also {
                Logger.info { "taxa last synchronization date: ${it.toIsoDateString()}" }
            }
            val lastUpdatedDateFromAPIs = runCatching {
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

            if (!hasLocalData || lastUpdatedDate == null || lastUpdatedDateFromAPIs == null || lastUpdatedDateFromAPIs.after(lastUpdatedDate)) {
                Logger.info { "synchronize taxa..." }

                runCatching {
                    database
                        .taxonDao()
                        .deleteAll()
                }.onFailure { Logger.warn(it) { "failed to deleting existing taxa" } }

                synchronizeTaxaWithPagination(params.pageSize)
                    .mapLatest {
                        if (it.state == WorkInfo.State.SUCCEEDED) updateTaxaLastUpdatedDate()
                        it
                    }
                    .collect {
                        sendOrThrow(
                            this,
                            it
                        )
                    }
            }

            if (hasLocalData && lastUpdatedDateFromAPIs?.before(lastUpdatedDate) == true) {
                val taxrefList = runCatching {
                    geoNatureAPIClient
                        .getTaxrefList()
                        .await().data
                }.getOrDefault(emptyList())

                if (taxrefList.isNotEmpty()) {
                    Logger.info { "synchronize taxa list..." }

                    runCatching {
                        database
                            .taxonListDao()
                            .deleteAll()
                    }.onFailure { Logger.warn(it) { "failed to deleting existing taxa list" } }

                    synchronizeTaxaWithPagination(params.pageSize,
                        taxrefList.map { it.id }).collect {
                        sendOrThrow(
                            this,
                            it
                        )
                    }
                }
            }

            if (params.withAdditionalData && params.codeAreaType?.isNotBlank() == true) {
                Logger.info { "synchronize taxa additional data..." }

                synchronizeTaxaAdditionalDataWithPagination(
                    params.pageSize,
                    params.codeAreaType
                ).collect {
                    sendOrThrow(
                        this,
                        it
                    )
                }
            }
        }

    private suspend fun synchronizeTaxaWithPagination(
        pageSize: Int,
        list: List<Long>? = null
    ): Flow<DataSyncStatus> =
        flow {
            var hasNext: Boolean
            var offset = 0
            var page = 1
            var hasErrors = false

            // fetch all taxa from paginated list
            do {
                val taxrefListResult = runCatching {
                    geoNatureAPIClient
                        .getTaxref(
                            pageSize,
                            page,
                            list
                        )
                        .await()
                }
                    .onFailure {
                        Logger.warn { "taxa synchronization finished with errors" }
                        hasErrors = true
                        emit(
                            DataSyncStatus.fromException(
                                it,
                                context,
                                context.getString(R.string.sync_data_taxa_with_errors)
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
                        Taxon(
                            id = taxRef.id,
                            name = taxRef.name.trim(),
                            taxonomy = Taxonomy(
                                taxRef.kingdom
                                    ?: Taxonomy.ANY,
                                taxRef.group
                                    ?: Taxonomy.ANY
                            ),
                            commonName = taxRef.commonName?.trim(),
                            description = taxRef.fullName?.trim()
                        )
                    }
                    .toList()

                val taxaList = taxrefListResult.items
                    .asSequence()
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

                runCatching {
                    database
                        .taxonDao()
                        .insertAll(taxa)
                    database
                        .taxonListDao()
                        .insertAll(taxaList)
                }.onFailure {
                    Logger.warn(it) { "failed to update taxa (page: $page)" }
                    hasErrors = true
                }

                Logger.info { "taxa to update: ${offset + taxa.size}" }

                emit(
                    DataSyncStatus(
                        state = WorkInfo.State.SUCCEEDED,
                        syncMessage = context.getString(
                            R.string.sync_data_taxa,
                            (offset + taxa.size)
                        )
                    )
                )

                offset += pageSize
                page++
                hasNext = taxrefListResult.items.size == pageSize
            } while (hasNext && !hasErrors)
        }

    private suspend fun synchronizeTaxaAdditionalDataWithPagination(
        pageSize: Int,
        codeAreaType: String?
    ): Flow<DataSyncStatus> =
        flow {
            var hasNext: Boolean
            var offset = 0
            var page = 1

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
                            DataSyncStatus.fromException(
                                it,
                                context,
                                context.getString(R.string.sync_data_taxa_areas_with_errors)
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
                        syncMessage = context.getString(
                            R.string.sync_data_taxa_areas,
                            (offset + taxrefAreasResponse.size)
                        )
                    )
                )

                val taxonAreas = taxrefAreasResponse
                    .asSequence()
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

                runCatching {
                    database
                        .taxonAreaDao()
                        .insertAll(taxonAreas)
                }.onFailure { Logger.warn(it) { "failed to update taxa with areas (page: $page)" } }

                Logger.info { "updating ${taxonAreas.size} taxa with areas from page $page" }

                offset += pageSize
                page++
                hasNext = taxrefAreasResponse.size == pageSize
            } while (hasNext)
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

    companion object {
        private const val KEY_SYNC_TAXA_LAST_UPDATED_AT = "key_sync_taxa_last_updated_at"
    }
}