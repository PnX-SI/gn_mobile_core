package fr.geonature.sync.sync

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.datasync.api.IGeoNatureAPIClient
import javax.inject.Singleton

/**
 * Data synchronization module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Singleton
    @Provides
    fun providePackageInfoManager(
        @ApplicationContext appContext: Context,
        geoNatureAPIClient: IGeoNatureAPIClient,
    ): IPackageInfoManager {
        return PackageInfoManagerImpl(
            appContext,
            geoNatureAPIClient
        )
    }
}