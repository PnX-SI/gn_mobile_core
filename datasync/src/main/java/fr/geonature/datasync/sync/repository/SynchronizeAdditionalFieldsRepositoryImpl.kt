package fr.geonature.datasync.sync.repository

import android.content.Context
import androidx.work.WorkInfo
import fr.geonature.commons.features.dataset.data.IDatasetLocalDataSource
import fr.geonature.commons.features.nomenclature.data.IAdditionalFieldLocalDataSource
import fr.geonature.datasync.R
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.sync.DataSyncStatus
import fr.geonature.datasync.sync.io.AdditionalFieldJsonReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.tinylog.Logger
import retrofit2.await
import java.io.BufferedReader

/**
 * Default repository interface to synchronize additional fields.
 */
interface ISynchronizeAdditionalFieldsRepository : ISynchronizeLocalDataRepository<Unit>

/**
 * Implementation of [ISynchronizeAdditionalFieldsRepository] to synchronize additional fields.
 *
 * @author S. Grimault
 */
class SynchronizeAdditionalFieldsRepositoryImpl(
    private val context: Context,
    private val moduleName: String,
    private val datasetLocalDataSource: IDatasetLocalDataSource,
    private val additionalFieldLocalDataSource: IAdditionalFieldLocalDataSource,
    private val geoNatureAPIClient: IGeoNatureAPIClient
) : ISynchronizeAdditionalFieldsRepository {

    override suspend fun invoke(params: Unit): Flow<DataSyncStatus> =
        flow {
            Logger.info { "synchronize additional fields..." }

            val additionalFieldJsonReader = AdditionalFieldJsonReader()

            val additionalFields = runCatching {
                geoNatureAPIClient
                    .getAdditionalFields(moduleName.uppercase())
                    .await()
                    .let {
                        additionalFieldJsonReader.read(
                            it
                                .byteStream()
                                .bufferedReader()
                                .use(BufferedReader::readText)
                        )
                    }
            }
                .onFailure { Logger.warn { it.message } }
                .getOrDefault(emptyList())

            if (additionalFields.isEmpty()) {
                emit(DataSyncStatus(state = WorkInfo.State.SUCCEEDED))

                return@flow
            }

            Logger.info { "${additionalFields.size} additional field(s) found from API" }

            // keep only additional fields with valid datasets
            val datasetIds = datasetLocalDataSource
                .getAllDatasets()
                .map { it.id }
            val validAdditionalFields = additionalFields.mapNotNull {
                if (it.datasetIds.isEmpty()) it
                else it.datasetIds
                    .filter { id -> datasetIds.contains(id) }
                    .takeIf { ids -> ids.isNotEmpty() }
                    ?.let { ids -> it.copy(datasetIds = ids) }
            }

            Logger.info { "updating ${validAdditionalFields.size} valid additional field(s)..." }

            runCatching {
                additionalFieldLocalDataSource.updateAdditionalFields(*validAdditionalFields.toTypedArray())
            }.onFailure {
                emit(
                    DataSyncStatus(
                        state = WorkInfo.State.FAILED,
                        syncMessage = context.getString(R.string.sync_data_additional_fields_error)
                    )
                )
            }

            emit(
                DataSyncStatus(
                    state = WorkInfo.State.SUCCEEDED,
                    syncMessage = context.getString(
                        R.string.sync_data_additional_fields,
                        validAdditionalFields.size
                    )
                )
            )
        }
}