package fr.geonature.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideGeoNatureApiClient(cookieManager: ICookieManager): IGeoNatureAPIClient {
        return GeoNatureAPIClientImpl(cookieManager)
    }
}