package fr.geonature.datasync.settings

import androidx.lifecycle.LiveData
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.datasync.api.IGeoNatureAPIClient

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
     * Loads locally [DataSyncSettings].
     *
     * @return [DataSyncSettings] or [Failure] if something goes wrong
     */
    suspend fun getDataSyncSettings(): Either<Failure, DataSyncSettings>

    /**
     * Returns the current base URLs.
     *
     * @return [IGeoNatureAPIClient.ServerUrls] or [Failure] if none was configured.
     */
    fun getServerBaseUrls(): Either<Failure, IGeoNatureAPIClient.ServerUrls>

    /**
     * Sets server base URL and updates the current [DataSyncSettings].
     */
    fun setServerBaseUrl(geoNatureServerUrl: String)
}