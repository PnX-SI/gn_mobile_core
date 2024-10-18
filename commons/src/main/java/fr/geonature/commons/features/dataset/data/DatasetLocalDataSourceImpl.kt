package fr.geonature.commons.features.dataset.data

import fr.geonature.commons.data.dao.DatasetDao
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.features.dataset.error.DatasetException

/**
 * Default implementation of [IDatasetLocalDataSource] using local database.
 *
 * @author S. Grimault
 */
class DatasetLocalDataSourceImpl(private val datasetDao: DatasetDao) : IDatasetLocalDataSource {
    override suspend fun findDatasetById(datasetId: Long): Dataset {
        return datasetDao.findById(datasetId)
            ?: throw DatasetException.NoDatasetFoundException(datasetId)
    }
}