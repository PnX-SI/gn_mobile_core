package fr.geonature.sync.di

import android.app.Application
import fr.geonature.sync.sync.IPackageInfoManager
import fr.geonature.sync.sync.PackageInfoManagerImpl

/**
 * Service Locator
 *
 * @author S. Grimault
 */
class ServiceLocator(private val application: Application) {

    private var packageInfoManager: IPackageInfoManager? = null

    fun providePackageInfoManager(): IPackageInfoManager {
        if (packageInfoManager == null) {
            packageInfoManager = PackageInfoManagerImpl(application)
        }

        return packageInfoManager!!
    }
}