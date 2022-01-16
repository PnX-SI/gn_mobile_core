package fr.geonature.datasync.settings

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.FileUtils.getFile
import fr.geonature.mountpoint.util.FileUtils.getRootFolder
import javax.inject.Singleton

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
    fun provideDataSyncSettingsDataSource(@ApplicationContext appContext: Context): IDataSyncSettingsDataSource {
        return DataSyncSettingsJsonDataSource(
            getFile(
                getRootFolder(
                    appContext,
                    MountPoint.StorageType.INTERNAL
                ),
                "settings_${appContext.packageName.substring(appContext.packageName.lastIndexOf('.') + 1)}.json"
            )
        )
    }

    @Singleton
    @Provides
    fun provideDataSyncSettingsRepository(
        @ApplicationContext appContext: Context,
        dataSyncSettingsDataSource: IDataSyncSettingsDataSource
    ): IDataSyncSettingsRepository {
        return DataSyncSettingsRepositoryImpl(
            appContext,
            dataSyncSettingsDataSource
        )
    }
}