package fr.geonature.commons.features.dataset.repository

import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.features.dataset.data.IDatasetLocalDataSource

/**
 * Default implementation of [IDatasetRepository].
 *
 * @author S. Grimault
 */
class DatasetRepositoryImpl(private val datasetLocalDataSource: IDatasetLocalDataSource) :
    IDatasetRepository {

    override suspend fun getDatasetById(datasetId: Long): Result<Dataset> {
        return runCatching {
            datasetLocalDataSource.findDatasetById(datasetId)
        }
    }
}