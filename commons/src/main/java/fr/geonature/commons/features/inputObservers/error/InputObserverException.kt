package fr.geonature.commons.features.inputObservers.error

import fr.geonature.commons.data.entity.InputObserver

/**
 * Base exception about [InputObserver].
 *
 * @author S. Grimault
 */
sealed class InputObserverException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {
    /**
     * Thrown if no [InputObserver] was found locally from a given IDs.
     */
    data class NoInputObserversFoundException(var id: List<Long>) :
        InputObserverException("no input observer found with IDs ${id.joinToString(", ")}")
}