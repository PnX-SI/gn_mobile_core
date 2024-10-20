package fr.geonature.commons.features.nomenclature.repository

import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.NomenclatureWithType
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource

/**
 * Default implementation of [INomenclatureRepository].
 *
 * @author S. Grimault
 */
open class NomenclatureRepositoryImpl(private val nomenclatureLocalDataSource: INomenclatureLocalDataSource) :
    INomenclatureRepository {

    override suspend fun getAllNomenclatureTypes(): Result<List<NomenclatureType>> {
        return runCatching {
            nomenclatureLocalDataSource.getAllNomenclatureTypes()
        }
    }

    override suspend fun getAllDefaultNomenclatureValues(): Result<List<NomenclatureWithType>> {
        return runCatching {
            val nomenclatureTypes = nomenclatureLocalDataSource.getAllNomenclatureTypes()
            nomenclatureLocalDataSource
                .getAllDefaultNomenclatureValues()
                .map { nomenclature ->
                    NomenclatureWithType(nomenclature).apply {
                        type = nomenclatureTypes.firstOrNull { it.id == nomenclature.typeId }
                    }
                }
                .filterNot { it.type == null }
        }
    }

    override suspend fun getNomenclatureValuesByTypeAndTaxonomy(
        mnemonic: String,
        taxonomy: Taxonomy?
    ): Result<List<Nomenclature>> {
        return runCatching {
            nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                mnemonic,
                taxonomy
            )
        }
    }
}