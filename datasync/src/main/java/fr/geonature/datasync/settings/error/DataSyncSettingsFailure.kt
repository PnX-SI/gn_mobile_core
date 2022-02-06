package fr.geonature.datasync.error

import fr.geonature.commons.fp.Failure
import fr.geonature.datasync.settings.DataSyncSettings

/**
 * Failure about [DataSyncSettings] not loaded from given source.
 *
 * @author S. Grimault
 */
data class DataSyncSettingsNotFoundFailure(val source: String? = null) : Failure.FeatureFailure()

/**
 * [DataSyncSettings] `JSON` parse error.
 *
 * @author S. Grimault
 */
object DataSyncSettingsJsonParseFailure : Failure.FeatureFailure()