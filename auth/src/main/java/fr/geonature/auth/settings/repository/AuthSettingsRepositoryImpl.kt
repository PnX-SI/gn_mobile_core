package fr.geonature.auth.settings.repository

import fr.geonature.auth.settings.data.IAuthSettingsDataSource
import fr.geonature.auth.settings.domain.AuthSettings

/**
 * Default implementation of [IAuthSettingsRepository].
 *
 * @author S. Grimault
 */
class AuthSettingsRepositoryImpl(private val authSettingsDataSource: IAuthSettingsDataSource) :
    IAuthSettingsRepository {

    override suspend fun getAuthSettings(): Result<AuthSettings> {
        return runCatching { authSettingsDataSource.load() }
    }
}
