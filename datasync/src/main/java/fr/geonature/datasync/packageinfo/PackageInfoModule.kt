package fr.geonature.datasync.packageinfo

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.settings.AppSettingsFilename
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class AvailablePackageInfoDataSource

@Qualifier
annotation class InstalledPackageInfoDataSource

/**
 * Manage installed [PackageInfo].
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object PackageInfoModule {

    @Singleton
    @Provides
    @AvailablePackageInfoDataSource
    fun provideAvailablePackageInfoDataSource(geoNatureAPIClient: IGeoNatureAPIClient): IPackageInfoDataSource {
        return AvailablePackageInfoDataSourceImpl(geoNatureAPIClient)
    }

    @Singleton
    @Provides
    @InstalledPackageInfoDataSource
    fun provideInstalledPackageInfoDataSource(@ApplicationContext appContext: Context): IPackageInfoDataSource {
        return InstalledPackageInfoDataSourceImpl(appContext)
    }

    @Singleton
    @Provides
    fun providePackageInfoRepository(
        @ApplicationContext appContext: Context,
        @AvailablePackageInfoDataSource availablePackageInfoDataSource: IPackageInfoDataSource,
        @InstalledPackageInfoDataSource installedPackageInfoDataSource: IPackageInfoDataSource,
        @AppSettingsFilename appSettingsFilename: String
    ): IPackageInfoRepository {
        return PackageInfoRepositoryImpl(
            appContext,
            availablePackageInfoDataSource = availablePackageInfoDataSource,
            installedPackageInfoDataSource = installedPackageInfoDataSource,
            appSettingsFilename
        )
    }
}