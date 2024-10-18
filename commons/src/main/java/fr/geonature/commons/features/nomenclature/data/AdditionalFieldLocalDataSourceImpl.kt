package fr.geonature.commons.features.nomenclature.data

import androidx.room.withTransaction
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.entity.AdditionalFieldDataset
import fr.geonature.commons.data.entity.AdditionalFieldNomenclature
import fr.geonature.commons.data.entity.AdditionalFieldWithValues

/**
 * Default implementation of [IAdditionalFieldLocalDataSource] using local database.
 *
 * @author S. Grimault
 */
class AdditionalFieldLocalDataSourceImpl(private val database: LocalDatabase) :
    IAdditionalFieldLocalDataSource {

    override suspend fun getAdditionalFields(
        datasetId: Long?,
        vararg codeObject: String
    ): List<AdditionalFieldWithValues> {
        return (database
            .additionalFieldDao()
            .findAllWithNomenclatureByCodeObject(*codeObject)
            .map {
                AdditionalFieldWithValues(
                    additionalField = it.additionalField,
                    nomenclatureTypeMnemonic = it.mnemonic,
                    codeObjects = listOf(it.codeObject),
                )
            } + (datasetId?.let {
            database
                .additionalFieldDao()
                .findAllWithNomenclatureByDatasetAndCodeObject(
                    datasetId,
                    *codeObject
                )
                .map {
                    AdditionalFieldWithValues(
                        additionalField = it.additionalField,
                        datasetIds = listOf(datasetId),
                        nomenclatureTypeMnemonic = it.mnemonic,
                        codeObjects = listOf(it.codeObject),
                    )
                }
        }
            ?: emptyList()) + database
            .additionalFieldDao()
            .findAllByCodeObject(*codeObject)
            .map {
                AdditionalFieldWithValues(
                    additionalField = it.key.additionalField,
                    codeObjects = listOf(it.key.codeObject),
                    values = it.value
                )
            } + (datasetId?.let {
            database
                .additionalFieldDao()
                .findAllByDatasetAndCodeObject(
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
                                datasetId = datasetId
                            )
                        }
                    }
                    .toTypedArray())
            database
                .additionalFieldNomenclatureDao()
                .insert(*additionalFieldWithValues
                    .mapNotNull { additionalFieldWithValues ->
                        additionalFieldWithValues.nomenclatureTypeMnemonic?.let {
                            AdditionalFieldNomenclature(
                                additionalFieldWithValues.additionalField.id,
                                it
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