package fr.geonature.datasync.sync

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.dao.AppSyncDao
import javax.inject.Singleton

/**
 * Data synchronization module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object DataSyncModule {

    @Singleton
    @Provides
    fun provideDataSyncManager(appSyncDao: AppSyncDao): IDataSyncManager {
        return DataSyncManagerImpl(appSyncDao)
    }
}