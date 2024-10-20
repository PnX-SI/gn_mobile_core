package fr.geonature.commons.features.nomenclature.repository

import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.NomenclatureWithType
import fr.geonature.commons.data.entity.Taxonomy

/**
 * Editable nomenclature types repository.
 *
 * @author S. Grimault
 */
interface INomenclatureRepository {

    /**
     * Gets all [NomenclatureType].
     *
     * @return a list of [NomenclatureType] or [Result.Failure] if something goes wrong
     */
    suspend fun getAllNomenclatureTypes(): Result<List<NomenclatureType>>

    /**
     * Gets all [Nomenclature] as default nomenclature values.
     *
     * @return a list of default [Nomenclature] or [Result.Failure] if something goes wrong
     */
    suspend fun getAllDefaultNomenclatureValues(): Result<List<NomenclatureWithType>>

    /**
     * Gets all nomenclature values matching given nomenclature type and an optional taxonomy rank.
     *
     * @param mnemonic the nomenclature type as main filter
     * @param taxonomy the taxonomy rank
     *
     * @return a list of [Nomenclature] matching given criteria or [Result.Failure] if something goes wrong
     */
    suspend fun getNomenclatureValuesByTypeAndTaxonomy(
        mnemonic: String,
        taxonomy: Taxonomy? = null
    ): Result<List<Nomenclature>>
}