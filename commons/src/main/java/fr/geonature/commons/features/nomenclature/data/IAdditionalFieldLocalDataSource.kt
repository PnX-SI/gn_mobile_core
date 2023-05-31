package fr.geonature.commons.features.nomenclature.data

import fr.geonature.commons.data.entity.AdditionalFieldWithValues

/**
 * [AdditionalFieldWithValues] local data source.
 *
 * @author S. Grimault
 */
interface IAdditionalFieldLocalDataSource {

    /**
     * Gets additional fields matching the code object as main filter.
     *
     * @param datasetId the selected dataset
     * @param codeObject code object as main filter
     *
     * @return a list of [AdditionalFieldWithValues]
     */
    suspend fun getAdditionalFields(
        datasetId: Long? = null,
        vararg codeObject: String
    ): List<AdditionalFieldWithValues>

    /**
     * Adds all given additional fields by removing all existing ones.
     */
    suspend fun updateAdditionalFields(vararg additionalFieldWithValues: AdditionalFieldWithValues)
}