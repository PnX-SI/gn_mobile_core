package fr.geonature.commons.features.inputObservers

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.dao.InputObserverDao
import fr.geonature.commons.features.inputObservers.data.IInputObserverLocalDataSource
import fr.geonature.commons.features.inputObservers.data.InputObserverLocalDataSourceImpl
import fr.geonature.commons.features.inputObservers.repository.IInputObserverRepository
import fr.geonature.commons.features.inputObservers.repository.InputObserverRepositoryImpl
import javax.inject.Singleton

/**
 * Input observer module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object InputObserverModule {

    @Singleton
    @Provides
    fun provideInputObserverLocalDataSource(inputObserverDao: InputObserverDao): IInputObserverLocalDataSource {
        return InputObserverLocalDataSourceImpl(inputObserverDao)
    }

    @Singleton
    @Provides
    fun provideInputObserverRepository(inputObserverLocalDataSource: IInputObserverLocalDataSource): IInputObserverRepository {
        return InputObserverRepositoryImpl(inputObserverLocalDataSource)
    }
}