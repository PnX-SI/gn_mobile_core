package fr.geonature.commons.settings

/**
 * Manage [IAppSettings].
 * - Read [IAppSettings] from URI
 * - Read [IAppSettings] from `JSON` file as fallback
 *
 * @author S. Grimault
 */
interface IAppSettingsManager<AS : IAppSettings> {
    fun getAppSettingsFilename(): String

    /**
     * Loads [IAppSettings] from URI or `JSON` file as fallback.
     *
     * @return [IAppSettings] or `null` if not found
     */
    suspend fun loadAppSettings(): AS?
}