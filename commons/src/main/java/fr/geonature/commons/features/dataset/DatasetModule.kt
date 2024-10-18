package fr.geonature.commons.features.dataset

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.dao.DatasetDao
import fr.geonature.commons.features.dataset.data.DatasetLocalDataSourceImpl
import fr.geonature.commons.features.dataset.data.IDatasetLocalDataSource
import fr.geonature.commons.features.dataset.repository.DatasetRepositoryImpl
import fr.geonature.commons.features.dataset.repository.IDatasetRepository
import javax.inject.Singleton

/**
 * Dataset module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object DatasetModule {

    @Singleton
    @Provides
    fun provideDatasetLocalDataSource(datasetDao: DatasetDao): IDatasetLocalDataSource {
        return DatasetLocalDataSourceImpl(datasetDao)
    }

    @Singleton
    @Provides
    fun provideDatasetRepository(datasetLocalDataSource: IDatasetLocalDataSource): IDatasetRepository {
        return DatasetRepositoryImpl(datasetLocalDataSource)
    }
}