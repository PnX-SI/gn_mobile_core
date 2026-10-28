package fr.geonature.auth.settings.data

import fr.geonature.auth.settings.domain.AuthSettings
import fr.geonature.auth.settings.error.AuthSettingsException

/**
 * Loads [AuthSettings].
 *
 * @author S. Grimault
 */
interface IAuthSettingsDataSource {

    /**
     * Loads [AuthSettings].
     *
     * @return [AuthSettings] or throws [AuthSettingsException] if something goes wrong.
     */
    suspend fun load(): AuthSettings
}
