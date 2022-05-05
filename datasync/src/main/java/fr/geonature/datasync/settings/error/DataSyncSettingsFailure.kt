package fr.geonature.datasync.settings.error

import fr.geonature.commons.error.Failure
import fr.geonature.datasync.settings.DataSyncSettings

/**
 * Failure about [DataSyncSettings] not loaded from given source.
 *
 * @author S. Grimault
 */
data class DataSyncSettingsNotFoundFailure(
    val source: String? = null,
    val geoNatureBaseUrl: String? = null,
) : Failure.FeatureFailure()

/**
 * [DataSyncSettings] `JSON` parse error.
 *
 * @author S. Grimault
 */
object DataSyncSettingsJsonParseFailure : Failure.FeatureFailure()