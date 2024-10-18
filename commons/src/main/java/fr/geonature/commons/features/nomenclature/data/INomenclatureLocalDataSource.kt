package fr.geonature.commons.features.nomenclature.data

import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.nomenclature.error.NomenclatureException

/**
 * [NomenclatureType], [Nomenclature] local data source.
 *
 * @author S. Grimault
 */
interface INomenclatureLocalDataSource {

    /**
     * Gets all [NomenclatureType].
     *
     * @return a list of [NomenclatureType]
     * @throws NomenclatureException.NoNomenclatureTypeFoundException if no nomenclature types was found
     */
    suspend fun getAllNomenclatureTypes(): List<NomenclatureType>

    /**
     * Gets all [Nomenclature] as default nomenclature values.
     *
     * @return a list of default [Nomenclature]
     */
    suspend fun getAllDefaultNomenclatureValues(): List<Nomenclature>

    /**
     * Gets all nomenclature values matching given nomenclature type and an optional taxonomy rank.
     *
     * @param mnemonic the nomenclature type as main filter
     * @param taxonomy the taxonomy rank
     *
     * @return a list of [Nomenclature] matching given criteria
     * @throws NomenclatureException.NoNomenclatureValuesFoundException if no nomenclature values was found
     */
    suspend fun getNomenclatureValuesByTypeAndTaxonomy(
        mnemonic: String,
        taxonomy: Taxonomy? = null
    ): List<Nomenclature>
}