package fr.geonature.sync.auth

import fr.geonature.commons.fp.Failure
import fr.geonature.sync.api.model.AuthLoginError

/**
 * Authentication failure.
 *
 * @author S. Grimault
 */
data class AuthFailure(val authLoginError: AuthLoginError) : Failure.FeatureFailure()