package fr.geonature.datasync.packageinfo.error

import fr.geonature.datasync.packageinfo.PackageInfo
/**
 * Base exception about [PackageInfo].
 *
 * @author S. Grimault
 */
sealed class PackageInfoException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {

    /**
     * Thrown when a [PackageInfo] has no settings.
     */
    data class MissingSettingsException(val packageName: String) : PackageInfoException(
        "missing settings from PackageInfo '$packageName'"
    )
}