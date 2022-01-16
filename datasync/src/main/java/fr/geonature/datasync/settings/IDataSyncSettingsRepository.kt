package fr.geonature.datasync.settings

import androidx.lifecycle.LiveData
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Failure

/**
 * Manage [DataSyncSettings]:
 * - Read [DataSyncSettings] locally.
 * - Update [DataSyncSettings] from user preferences.
 *
 * @author S. Grimault
 */
interface IDataSyncSettingsRepository {

    /**
     * The current [DataSyncSettings] loaded.
     */
    val dataSyncSettings: LiveData<Either<Failure, DataSyncSettings>>

    /**
     * Returns the current [DataSyncSettings] loaded or tries to load it from data source.
     *
     * @return [DataSyncSettings] or [Failure] if something goes wrong
     */
    suspend fun getDataSyncSettings(): Either<Failure, DataSyncSettings>

    /**
     * Sets server base URLs and updates the current [DataSyncSettings].
     */
    fun setServerUrls(
        geoNatureServerUrl: String,
        taxHubServerUrl: String
    )
}