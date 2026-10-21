package fr.geonature.datasync.auth.error

import fr.geonature.api.model.AuthLoginError
import fr.geonature.commons.error.Failure

/**
 * Authentication failure.
 *
 * @author S. Grimault
 */
sealed class AuthFailure : Failure.FeatureFailure() {
    data class AuthLoginFailure(val authLoginError: AuthLoginError) : AuthFailure()
    object InvalidUserFailure : AuthFailure()
}
