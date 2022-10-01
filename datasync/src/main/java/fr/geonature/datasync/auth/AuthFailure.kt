package fr.geonature.datasync.auth

import fr.geonature.commons.error.Failure
import fr.geonature.datasync.api.model.AuthLoginError

/**
 * Authentication failure.
 *
 * @author S. Grimault
 */
sealed class AuthFailure : Failure.FeatureFailure() {
    data class AuthLoginFailure(val authLoginError: AuthLoginError) : AuthFailure()
    object InvalidUserFailure : AuthFailure()
}
