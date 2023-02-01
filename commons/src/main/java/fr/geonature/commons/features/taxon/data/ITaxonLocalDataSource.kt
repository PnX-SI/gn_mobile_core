package fr.geonature.commons.features.taxon.data

import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.TaxonWithArea
import fr.geonature.commons.features.taxon.error.TaxonException

/**
 * [Taxon], [TaxonArea], [TaxonWithArea] local data source.
 *
 * @author S. Grimault
 */
interface ITaxonLocalDataSource {

    /**
     * Finds [Taxon] matching given taxon ID.
     *
     * @param taxonId the [Taxon] identifier to find
     *
     * @return a [Taxon] found from given ID
     * @throws TaxonException.NoTaxonFoundException if not found
     */
    suspend fun findTaxonById(taxonId: Long): Taxon

    /**
     * Finds a list of taxa matching given IDs.
     *
     * @param taxonIds a list of taxa identifiers to find
     *
     * @return a list of taxa found from given ID
     */
    suspend fun findTaxaByIds(vararg taxonIds: Long): List<Taxon>

    /**
     * Finds [TaxonWithArea] matching the given taxon ID and the area ID.
     *
     * @param taxonId the [Taxon] identifier to find
     * @param areaId the area ID as filter
     *
     * @return a [Taxon] found with [TaxonArea] if defined
     * @throws TaxonException.NoTaxonFoundException if not found
     */
    suspend fun findTaxonByIdWithArea(
        taxonId: Long,
        areaId: Long
    ): TaxonWithArea
}