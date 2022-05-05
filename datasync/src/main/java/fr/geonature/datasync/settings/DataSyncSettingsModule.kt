package fr.geonature.datasync.settings

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.ContentProviderAuthority
import fr.geonature.commons.data.helper.ProviderHelper.buildUri
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.FileUtils.getFile
import fr.geonature.mountpoint.util.FileUtils.getRootFolder
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class FileDataSource

@Qualifier
annotation class UriDataSource

/**
 * App settings filename.
 */
@MustBeDocumented
@Qualifier
annotation class AppSettingsFilename

/**
 * Data synchronization settings module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object DataSyncSettingsModule {

    @Singleton
    @Provides
    @FileDataSource
    fun provideDataSyncSettingsFileDataSource(
        @ApplicationContext appContext: Context,
        @AppSettingsFilename appSettingsFilename: String
    ): IDataSyncSettingsDataSource {
        return DataSyncSettingsFileDataSourceImpl(
            getFile(
                getRootFolder(
                    appContext,
                    MountPoint.StorageType.INTERNAL
                ),
                appSettingsFilename
            )
        )
    }

    @Singleton
    @Provides
    @UriDataSource
    fun provideDataSyncSettingsUriDataSource(
        @ApplicationContext appContext: Context,
        @ContentProviderAuthority providerAuthority: String,
        @AppSettingsFilename appSettingsFilename: String
    ): IDataSyncSettingsDataSource {
        return DataSyncSettingsUriDataSourceImpl(
            appContext,
            buildUri(
                providerAuthority,
                "settings",
                appSettingsFilename
            )
        )
    }

    @Singleton
    @Provides
    fun provideDataSyncSettingsRepository(
        @ApplicationContext appContext: Context,
        @FileDataSource dataSyncSettingsDataSource: IDataSyncSettingsDataSource
    ): IDataSyncSettingsRepository {
        return DataSyncSettingsRepositoryImpl(
            appContext,
            dataSyncSettingsDataSource
        )
    }
}