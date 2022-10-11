package fr.geonature.commons.features.input.error

import fr.geonature.commons.error.Failure
import fr.geonature.commons.features.input.domain.AbstractInput

/**
 * Failure about [AbstractInput].
 *
 * @author S. Grimault
 */
sealed class InputFailure : Failure.FeatureFailure() {
    object Failure: InputFailure()
    data class IOFailure(val message: String) : InputFailure()
    data class NotFoundFailure(val message: String) : InputFailure()
}
