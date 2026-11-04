package fr.geonature.auth.error

import fr.geonature.api.model.AuthLoginError

/**
 * Base exception about authentication errors.
 *
 * @author S. Grimault
 */
sealed class AuthException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {

    /**
     * Handles not connected error.
     */
    class NotConnectedException : AuthException("not connected")

    /**
     * Handles authentication errors returned by the backend.
     */
    data class AuthLoginException(val authLoginError: AuthLoginError) :
        AuthException(authLoginError.message)

    /**
     * Handles invalid user received after authentication.
     */
    class InvalidUserException : AuthException("invalid user")
}
