package fr.geonature.datasync.sync

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.GeoNatureModuleName
import fr.geonature.commons.data.dao.AppSyncDao
import fr.geonature.commons.features.nomenclature.data.IAdditionalFieldLocalDataSource
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.sync.repository.ISynchronizeLocalDataRepository
import fr.geonature.datasync.sync.repository.SynchronizeAdditionalFieldsRepositoryImpl
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class SynchronizeAdditionalFieldsRepository

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
    @SynchronizeAdditionalFieldsRepository
    fun provideSynchronizeAdditionalFieldsRepository(
        @ApplicationContext appContext: Context,
        @GeoNatureModuleName moduleName: String,
        additionalFieldLocalDataSource: IAdditionalFieldLocalDataSource,
        geoNatureAPIClient: IGeoNatureAPIClient,
    ): ISynchronizeLocalDataRepository {
        return SynchronizeAdditionalFieldsRepositoryImpl(
            appContext,
            moduleName,
            additionalFieldLocalDataSource,
            geoNatureAPIClient
        )
    }

    @Singleton
    @Provides
    fun provideDataSyncManager(appSyncDao: AppSyncDao): IDataSyncManager {
        return DataSyncManagerImpl(appSyncDao)
    }
}