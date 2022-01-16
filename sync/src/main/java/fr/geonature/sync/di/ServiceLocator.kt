package fr.geonature.sync.di

import android.app.Application
import fr.geonature.commons.settings.AppSettingsManagerImpl
import fr.geonature.commons.settings.IAppSettingsManager
import fr.geonature.sync.settings.AppSettings
import fr.geonature.sync.settings.io.OnAppSettingsJsonReaderListenerImpl

/**
 * Service Locator
 *
 * @author S. Grimault
 */
@Deprecated("use instead Hilt as default dependency injection")
class ServiceLocator(private val application: Application) {

    val appSettingsManager: IAppSettingsManager<AppSettings> by lazy {
        AppSettingsManagerImpl(
            application,
            OnAppSettingsJsonReaderListenerImpl()
        )
    }
}