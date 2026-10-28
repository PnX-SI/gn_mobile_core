package fr.geonature.auth.settings.repository

import fr.geonature.auth.settings.domain.AuthSettings

/**
 * [AuthSettings] repository.
 *
 * @author S. Grimault
 */
interface IAuthSettingsRepository {

    /**
     * Gets the current [AuthSettings].
     *
     * @return [Result.success] with the loaded [AuthSettings] or [Result.failure] with an
     * [fr.geonature.auth.settings.error.AuthSettingsException] if something goes wrong
     */
    suspend fun getAuthSettings(): Result<AuthSettings>
}
