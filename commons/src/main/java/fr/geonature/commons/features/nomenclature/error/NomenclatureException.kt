package fr.geonature.commons.features.nomenclature.error

import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType

/**
 * Base exception about [NomenclatureType] and [Nomenclature].
 *
 * @author S. Grimault
 */
sealed class NomenclatureException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {
    /**
     * Failure about no [NomenclatureType] found locally.
     */
    object NoNomenclatureTypeFoundException : NomenclatureException()

    /**
     * Failure about no [Nomenclature] found from given mnemonic.
     */
    data class NoNomenclatureValuesFoundException(val mnemonic: String) : NomenclatureException()

    /**
     * Failure about no [Nomenclature] found.
     */
    class NoNomenclatureFoundException(message: String?) : NomenclatureException(message)
}
