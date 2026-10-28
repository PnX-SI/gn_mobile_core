package fr.geonature.auth.settings

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.auth.settings.data.AuthSettingsFileDataSourceImpl
import fr.geonature.auth.settings.data.IAuthSettingsDataSource
import fr.geonature.auth.settings.repository.AuthSettingsRepositoryImpl
import fr.geonature.auth.settings.repository.IAuthSettingsRepository
import fr.geonature.commons.settings.AppSettingsFilename
import fr.geonature.commons.util.getFile
import fr.geonature.commons.util.getPrimaryExternalStorage
import javax.inject.Singleton

/**
 * Authentication settings module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthSettingsModule {

    @Singleton
    @Provides
    fun provideAuthSettingsDataSource(
        @ApplicationContext appContext: Context,
        @AppSettingsFilename appSettingsFilename: String
    ): IAuthSettingsDataSource {
        return AuthSettingsFileDataSourceImpl(
            appContext
                .getPrimaryExternalStorage()
                .getFile(appSettingsFilename)
        )
    }

    @Singleton
    @Provides
    fun provideAuthSettingsRepository(authSettingsDataSource: IAuthSettingsDataSource): IAuthSettingsRepository {
        return AuthSettingsRepositoryImpl(authSettingsDataSource)
    }
}
