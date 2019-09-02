package fr.geonature.commons

import android.app.Application
import fr.geonature.commons.settings.IAppSettings

/**
 * Base class to maintain global application state.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MainApplication : Application() {
    private var appSettings: IAppSettings? = null

    fun <T : IAppSettings> getAppSettings(): T? {
        return appSettings as T?
    }

    fun <T : IAppSettings> setAppSettings(appSettings: T) {
        this.appSettings = appSettings
    }
}