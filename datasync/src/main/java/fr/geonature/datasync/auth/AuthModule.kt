package fr.geonature.datasync.auth

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.util.NetworkHandler
import fr.geonature.datasync.api.IGeoNatureAPIClient
import javax.inject.Singleton

/**
 * Authentication module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Singleton
    @Provides
    fun provideNetworkHandler(@ApplicationContext appContext: Context): NetworkHandler {
        return NetworkHandler(appContext)
    }

    @Singleton
    @Provides
    fun provideCookieManager(@ApplicationContext appContext: Context): ICookieManager {
        return CookieManagerImpl(appContext)
    }

    @Singleton
    @Provides
    fun provideAuthManager(
        @ApplicationContext appContext: Context,
        geoNatureAPIClient: IGeoNatureAPIClient,
        networkHandler: NetworkHandler
    ): IAuthManager {
        return AuthManagerImpl(
            appContext,
            geoNatureAPIClient,
            networkHandler
        )
    }
}