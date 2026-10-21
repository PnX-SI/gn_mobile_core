package fr.geonature.api

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.api.auth.CookieManagerImpl
import fr.geonature.api.auth.ICookieManager
import javax.inject.Singleton

/**
 * Api module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Singleton
    @Provides
    fun provideCookieManager(@ApplicationContext appContext: Context): ICookieManager {
        return CookieManagerImpl(appContext)
    }

    @Singleton
    @Provides
    fun provideGeoNatureApiClient(cookieManager: ICookieManager): IGeoNatureAPIClient {
        return GeoNatureAPIClientImpl(cookieManager)
    }
}