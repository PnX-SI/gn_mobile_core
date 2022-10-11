package fr.geonature.commons.features.input.error

import fr.geonature.commons.features.input.domain.AbstractInput

/**
 * Base exception about [AbstractInput].
 *
 * @author S. Grimault
 */
sealed class InputException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {
    /**
     * Thrown if no [AbstractInput] was found locally from a given ID.
     */
    data class NotFoundException(val id: Long) : InputException("no input found with ID '$id'")

    /**
     * Thrown if something goes wrong while reading [AbstractInput] from its given ID.
     */
    data class ReadException(val id: Long) :
        InputException("I/O Exception while reading input with ID '$id'")

    /**
     * Thrown if something goes wrong while writing [AbstractInput] from its given ID.
     */
    data class WriteException(val id: Long) :
        InputException("I/O Exception while writing input with ID '$id'")
}
