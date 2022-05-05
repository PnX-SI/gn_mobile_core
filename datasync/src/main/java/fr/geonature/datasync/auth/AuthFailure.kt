package fr.geonature.datasync.auth

import fr.geonature.commons.error.Failure
import fr.geonature.datasync.api.model.AuthLoginError

/**
 * Authentication failure.
 *
 * @author S. Grimault
 */
data class AuthFailure(val authLoginError: AuthLoginError) : Failure.FeatureFailure()