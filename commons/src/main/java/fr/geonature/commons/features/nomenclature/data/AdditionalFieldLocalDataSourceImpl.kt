package fr.geonature.commons.features.nomenclature.data

import androidx.room.withTransaction
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.entity.AdditionalFieldDataset
import fr.geonature.commons.data.entity.AdditionalFieldWithValues

/**
 * Default implementation of [IAdditionalFieldLocalDataSource] using local database.
 *
 * @author S. Grimault
 */
class AdditionalFieldLocalDataSourceImpl(
    private val moduleName: String,
    private val database: LocalDatabase,
) : IAdditionalFieldLocalDataSource {

    override suspend fun getAdditionalFields(
        datasetId: Long?,
        vararg codeObject: String
    ): List<AdditionalFieldWithValues> {
        return (database
            .additionalFieldDao()
            .findAllByModuleAndCodeObject(
                moduleName,
                *codeObject
            )
            .map {
                AdditionalFieldWithValues(
                    additionalField = it.key.additionalField,
                    codeObjects = listOf(it.key.codeObject),
                    values = it.value
                )
            } + (datasetId?.let {
            database
                .additionalFieldDao()
                .findAllByModuleAndDatasetAndCodeObject(
                    moduleName,
                    datasetId,
                    *codeObject
                )
                .map {
                    AdditionalFieldWithValues(
                        additionalField = it.key.additionalField,
                        datasetIds = listOf(datasetId),
                        codeObjects = listOf(it.key.codeObject),
                        values = it.value
                    )
                }
        }
            ?: emptyList())).fold(emptyList()) { acc, additionalFieldWithValues ->
            acc.filter { it.additionalField.id != additionalFieldWithValues.additionalField.id } + listOf(acc
                .firstOrNull { it.additionalField.id == additionalFieldWithValues.additionalField.id }
                ?.let {
                    it.copy(codeObjects = it.codeObjects + additionalFieldWithValues.codeObjects)
                }
                ?: additionalFieldWithValues)
        }
    }

    override suspend fun updateAdditionalFields(vararg additionalFieldWithValues: AdditionalFieldWithValues) {
        database.withTransaction {
            database
                .additionalFieldDao()
                .deleteAll()
            database
                .additionalFieldDao()
                .insert(*additionalFieldWithValues
                    .map { it.additionalField }
                    .toTypedArray())
            database
                .additionalFieldDatasetDao()
                .insert(*additionalFieldWithValues
                    .flatMap {
                        it.datasetIds.map { datasetId ->
                            AdditionalFieldDataset(
                                additionalFieldId = it.additionalField.id,
                                datasetId = datasetId,
                                module = moduleName
                            )
                        }
                    }
                    .toTypedArray())
            database
                .codeObjectDao()
                .insert(*additionalFieldWithValues
                    .flatMap { it.codeObjects }
                    .toTypedArray())
            database
                .fieldValueDao()
                .insert(*additionalFieldWithValues
                    .flatMap { it.values }
                    .toTypedArray())
        }
    }
}