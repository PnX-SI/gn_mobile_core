package fr.geonature.commons.features.dataset.repository

import fr.geonature.commons.data.entity.Dataset

/**
 * [Dataset] repository.
 *
 * @author S. Grimault
 */
interface IDatasetRepository {

    /**
     * Gets [Dataset] matching given taxon ID.
     *
     * @param datasetId the [Dataset] identifier to find
     *
     * @return a [Dataset] found from given ID or [Result.Failure] if something goes wrong
     */
    suspend fun getDatasetById(datasetId: Long): Result<Dataset>
}