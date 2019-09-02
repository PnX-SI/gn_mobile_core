package fr.geonature.commons.settings

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.geonature.commons.MainApplication
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [IAppSettings] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
abstract class AbstractAppSettingsViewModel<T : IAppSettings>(application: MainApplication) : AndroidViewModel(application) {

    internal val appSettingsManager: AppSettingsManager<T>
    private val appSettingsLiveData: MutableLiveData<T> = MutableLiveData()

    init {
        appSettingsManager = AppSettingsManager(application,
                                                this.getOnAppSettingsJsonReaderListener())
    }

    abstract fun getOnAppSettingsJsonReaderListener(): AppSettingsJsonReader.OnAppSettingsJsonReaderListener<T>

    fun getAppSettings(): LiveData<T> {
        load()
        return appSettingsLiveData
    }

    private fun load() {
        GlobalScope.launch(Dispatchers.Main) {
            val application = getApplication<MainApplication>()

            if (application.getAppSettings<T>() == null) {
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
}