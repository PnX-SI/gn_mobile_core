package fr.geonature.auth.settings.error

import fr.geonature.auth.settings.domain.AuthSettings
import java.io.IOException

/**
 * Base [IOException] about [AuthSettings].
 *
 * @author S. Grimault
 */
sealed class AuthSettingsException(
    message: String? = null,
    cause: Throwable? = null
) : IOException(
    message,
    cause
) {
    /**
     * Thrown if [AuthSettings] was not found.
     *
     * @author S. Grimault
     */
    data class AuthSettingsNotFoundException(
        val source: String? = null,
        override val cause: Throwable? = null
    ) : AuthSettingsException(
        if (source.isNullOrBlank()) "authentication settings not found"
        else "authentication settings not found from '$source'",
        cause
    )

    /**
     * Thrown if [AuthSettings] cannot be parsed from `JSON`.
     *
     * @author S. Grimault
     */
    data class AuthSettingsJsonParseException(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : AuthSettingsException(
        message,
        cause
    )
}
