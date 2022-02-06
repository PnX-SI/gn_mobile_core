package fr.geonature.datasync.settings.error

import fr.geonature.datasync.settings.DataSyncSettings
import java.io.IOException

/**
 * Base [IOException] about [DataSyncSettings].
 *
 * @author S. Grimault
 */
abstract class DataSyncSettingsException(
    message: String? = null,
    cause: Throwable? = null
) : IOException(
    message,
    cause
) {
    constructor(cause: Throwable) : this(
        null,
        cause
    )
}

/**
 * Thrown if [DataSyncSettings] was not found.
 *
 * @author S. Grimault
 */
class DataSyncSettingsNotFoundException(
    val source: String? = null,
    cause: Throwable? = null
) : DataSyncSettingsException(
    if (source.isNullOrBlank()) "data synchronization settings not found"
    else "data synchronization settings not found from '$source'",
    cause
)

/**
 * Thrown if [DataSyncSettings] cannot be parsed from `JSON`.
 *
 * @author S. Grimault
 */
class DataSyncSettingsJsonParseException(
    message: String? = null,
    cause: Throwable? = null
) : DataSyncSettingsException(
    message,
    cause
)