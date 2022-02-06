package fr.geonature.datasync.settings

import fr.geonature.datasync.settings.error.DataSyncSettingsException

/**
 * Loads [DataSyncSettings].
 *
 * @author S. Grimault
 */
interface IDataSyncSettingsDataSource {

    /**
     * Loads [DataSyncSettings].
     *
     * @return [DataSyncSettings] or throws [DataSyncSettingsException] if something goes wrong.
     */
    suspend fun load(): DataSyncSettings
}