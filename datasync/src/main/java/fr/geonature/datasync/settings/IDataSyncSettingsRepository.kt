package fr.geonature.datasync.settings

import androidx.lifecycle.LiveData
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Failure
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
     * Loads [DataSyncSettings] from data source.
     *
     * @return [DataSyncSettings] or [Failure] if something goes wrong
     */
    suspend fun getDataSyncSettings(): Either<Failure, DataSyncSettings>

    /**
     * Returns the current base URLs.
     */
    fun getServerBaseUrls(): Either<Failure, IGeoNatureAPIClient.ServerUrls>

    /**
     * Sets server base URLs and updates the current [DataSyncSettings].
     */
    fun setServerBaseUrls(
        geoNatureServerUrl: String,
        taxHubServerUrl: String
    )
}