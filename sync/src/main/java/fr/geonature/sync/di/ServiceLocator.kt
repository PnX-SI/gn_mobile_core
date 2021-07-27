package fr.geonature.sync.di

import android.app.Application
import fr.geonature.commons.util.NetworkHandler
import fr.geonature.sync.api.GeoNatureAPIClientImpl
import fr.geonature.sync.api.IGeoNatureAPIClient
import fr.geonature.sync.auth.AuthManagerImpl
import fr.geonature.sync.auth.CookieManagerImpl
import fr.geonature.sync.auth.IAuthManager
import fr.geonature.sync.sync.IPackageInfoManager
import fr.geonature.sync.sync.PackageInfoManagerImpl

/**
 * Service Locator
 *
 * @author S. Grimault
 */
class ServiceLocator(private val application: Application) {

    private val networkHandler: NetworkHandler by lazy {
        NetworkHandler(application)
    }

    val authManager: IAuthManager by lazy {
        AuthManagerImpl(
            application,
            geoNatureAPIClient,
            networkHandler
        )
    }

    val geoNatureAPIClient: IGeoNatureAPIClient by lazy {
        GeoNatureAPIClientImpl(
            application,
            CookieManagerImpl(application)
        )
    }

    val packageInfoManager: IPackageInfoManager by lazy {
        PackageInfoManagerImpl(
            application,
            geoNatureAPIClient
        )
    }
}