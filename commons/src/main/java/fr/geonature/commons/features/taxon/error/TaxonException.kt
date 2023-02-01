package fr.geonature.commons.features.taxon.error

import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.TaxonWithArea

/**
 * Base exception about [Taxon], [TaxonArea] and [TaxonWithArea].
 *
 * @author S. Grimault
 */
sealed class TaxonException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {
    /**
     * Failure about no [Taxon] or no [TaxonWithArea] found from given ID.
     */
    data class NoTaxonFoundException(val id: Long) :
        TaxonException("no taxon found with ID $id")
}
