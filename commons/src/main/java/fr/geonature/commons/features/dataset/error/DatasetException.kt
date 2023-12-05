package fr.geonature.commons.features.dataset.error

import fr.geonature.commons.data.entity.Dataset

/**
 * Base exception about [Dataset].
 *
 * @author S. Grimault
 */
sealed class DatasetException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {
    /**
     * Thrown if no [Dataset] was found locally from a given ID.
     */
    data class NoDatasetFoundException(val id: Long) :
        DatasetException("no dataset found with ID $id")
}