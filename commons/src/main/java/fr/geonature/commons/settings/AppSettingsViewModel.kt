package fr.geonature.commons.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import kotlinx.coroutines.launch

/**
 * [IAppSettings] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
open class AppSettingsViewModel<AS : IAppSettings>(
    application: Application,
    onAppSettingsJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<AS>
) : AndroidViewModel(application) {

    internal val appSettingsManager: AppSettingsManager<AS> = AppSettingsManager.getInstance(
        application,
        onAppSettingsJsonReaderListener
    )

    private val _appSettings: MutableLiveData<AS> = MutableLiveData()
    val appSettings: LiveData<AS> = _appSettings

    fun getAppSettingsFilename(): String {
        return appSettingsManager.getAppSettingsFilename()
    }

    fun <T> loadAppSettings() {
        viewModelScope.launch {
            appSettingsManager.loadAppSettings()?.also {
                _appSettings.postValue(it)
            }
        }
    }

    /**
     * Default Factory to use for [AppSettingsViewModel].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    class Factory<T : AppSettingsViewModel<AS>, AS : IAppSettings>(val creator: () -> T) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }
}
