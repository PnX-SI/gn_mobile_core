package fr.geonature.commons.features.dataset.data

import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.features.dataset.error.DatasetException

/**
 * [Dataset] local data source.
 *
 * @author S. Grimault
 */
interface IDatasetLocalDataSource {

    /**
     * Finds [Dataset] matching given taxon ID.
     *
     * @param datasetId the [Dataset] identifier to find
     *
     * @return a [Dataset] found from given ID
     * @throws DatasetException.NoDatasetFoundException if not found
     */
    suspend fun findDatasetById(datasetId: Long): Dataset
}