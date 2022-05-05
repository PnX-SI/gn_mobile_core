package fr.geonature.commons.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData

/**
 * [IAppSettings] view model.
 *
 * @author S. Grimault
 */
open class AppSettingsViewModel<AS : IAppSettings>(private val appSettingsManager: IAppSettingsManager<AS>) :
    ViewModel() {

    fun getAppSettingsFilename(): String {
        return appSettingsManager.getAppSettingsFilename()
    }

    fun loadAppSettings(): LiveData<AS?> =
        liveData {
            val appSettings = appSettingsManager.loadAppSettings()
            emit(appSettings)
        }

    /**
     * Default Factory to use for [AppSettingsViewModel].
     *
     * @author S. Grimault
     */
    class Factory<T : AppSettingsViewModel<AS>, AS : IAppSettings>(val creator: () -> T) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }
}
