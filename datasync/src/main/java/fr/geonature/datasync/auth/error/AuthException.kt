package fr.geonature.datasync.auth.error

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
    object NotConnectedException : AuthException("not connected")
}
