package fr.geonature.datasync.sync

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.GeoNatureModuleName
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.dao.AppSyncDao
import fr.geonature.commons.features.nomenclature.data.IAdditionalFieldLocalDataSource
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.sync.repository.ISynchronizeAdditionalFieldsRepository
import fr.geonature.datasync.sync.repository.ISynchronizeTaxaRepository
import fr.geonature.datasync.sync.repository.SynchronizeAdditionalFieldsRepositoryImpl
import fr.geonature.datasync.sync.repository.SynchronizeTaxaRepositoryImpl
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
    fun provideSynchronizeTaxaRepository(
        @ApplicationContext appContext: Context,
        geoNatureAPIClient: IGeoNatureAPIClient,
        database: LocalDatabase
    ): ISynchronizeTaxaRepository {
        return SynchronizeTaxaRepositoryImpl(
            appContext,
            geoNatureAPIClient,
            database
        )
    }

    @Singleton
    @Provides
    fun provideSynchronizeAdditionalFieldsRepository(
        @ApplicationContext appContext: Context,
        @GeoNatureModuleName moduleName: String,
        additionalFieldLocalDataSource: IAdditionalFieldLocalDataSource,
        geoNatureAPIClient: IGeoNatureAPIClient,
    ): ISynchronizeAdditionalFieldsRepository {
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