package fr.geonature.commons.settings.error

import fr.geonature.commons.settings.IAppSettings

/**
 * Base exception about [IAppSettings].
 *
 * @author S. Grimault
 */
sealed class AppSettingsException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {

    /**
     * Handles no [IAppSettings] found locally.
     */
    object NoAppSettingsFoundLocallyException : AppSettingsException("no app settings found locally")
}
