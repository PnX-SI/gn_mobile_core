package fr.geonature.commons.settings

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.geonature.commons.MainApplication
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import kotlinx.coroutines.launch

/**
 * [IAppSettings] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
open class AppSettingsViewModel<AS : IAppSettings>(application: MainApplication,
                                                   onAppSettingsJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<AS>) : AndroidViewModel(application) {

    internal val appSettingsManager: AppSettingsManager<AS> = AppSettingsManager(application,
                                                                                 onAppSettingsJsonReaderListener)
    private val appSettingsLiveData: MutableLiveData<AS> = MutableLiveData()

    fun getAppSettings(): LiveData<AS> {
        load()
        return appSettingsLiveData
    }

    private fun load() {
        viewModelScope.launch {
            val application = getApplication<MainApplication>()

            if (application.getAppSettings<AS>() == null) {
                val appSettings = appSettingsManager.loadAppSettings()

                if (appSettings != null) {
                    application.setAppSettings(appSettings)
                }

                appSettingsLiveData.postValue(appSettings)
            }
            else {
                appSettingsLiveData.postValue(application.getAppSettings())
            }
        }
    }

    /**
     * Default Factory to use for [AppSettingsViewModel].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    class Factory<T : AppSettingsViewModel<AS>, AS : IAppSettings>(val creator: () -> T) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }
}