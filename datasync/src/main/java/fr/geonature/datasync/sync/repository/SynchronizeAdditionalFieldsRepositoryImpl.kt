package fr.geonature.datasync.sync.repository

import android.content.Context
import androidx.work.WorkInfo
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

            Logger.info { "${additionalFields.size} additional field(s) found" }

            runCatching {
                additionalFieldLocalDataSource.updateAdditionalFields(*additionalFields.toTypedArray())
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
                        additionalFields.size
                    )
                )
            )
        }
}