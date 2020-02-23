package fr.geonature.commons.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import fr.geonature.mountpoint.model.MountPoint.StorageType.INTERNAL
import fr.geonature.mountpoint.util.FileUtils.getFile
import fr.geonature.mountpoint.util.FileUtils.getRootFolder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.IOException

/**
 * Manage [IAppSettings].
 * - Read [IAppSettings] from `JSON` file
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSettingsManager<AS : IAppSettings> private constructor(
    internal val application: Application,
    onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<AS>
) {

    private val appSettingsJsonReader: AppSettingsJsonReader<AS> =
        AppSettingsJsonReader(onAppSettingsJsonJsonReaderListener)

    private val _appSettings: MutableLiveData<AS> = MutableLiveData()
    val appSettings: LiveData<AS> = _appSettings

    init {
        GlobalScope.launch(Main) {
            withContext(IO) {
                getRootFolder(
                    application,
                    INTERNAL
                )
                    .mkdirs()
            }
        }
    }

    fun getAppSettingsFilename(): String {
        val packageName = application.packageName

        return "settings_${packageName.substring(packageName.lastIndexOf('.') + 1)}.json"
    }

    /**
     * Loads [IAppSettings] from `JSON` file.
     *
     * @return [IAppSettings] or `null` if not found
     */
    suspend fun loadAppSettings(): AS? = withContext(IO) {
        val currentLoadedAppSettings = _appSettings.value

        if (currentLoadedAppSettings == null) {
            val settingsJsonFile = getAppSettingsAsFile()

            Log.i(
                TAG,
                "Loading settings from '${settingsJsonFile.absolutePath}'..."
            )

            if (!settingsJsonFile.exists()) {
                Log.w(
                    TAG,
                    "'${settingsJsonFile.absolutePath}' not found"
                )
                null
            } else {
                try {
                    val appSettings = appSettingsJsonReader.read(FileReader(settingsJsonFile))

                    Log.i(
                        TAG,
                        "Settings loaded"
                    )

                    appSettings
                } catch (e: IOException) {
                    Log.w(
                        TAG,
                        "Failed to load '${settingsJsonFile.name}'"
                    )

                    null
                }
            }
        } else {
            currentLoadedAppSettings
        }.also { _appSettings.postValue(it) }
    }

    internal fun getAppSettingsAsFile(): File {
        return getFile(
            getRootFolder(
                application,
                INTERNAL
            ),
            getAppSettingsFilename()
        )
    }

    companion object {
        private val TAG = AppSettingsManager::class.java.name

        @Volatile
        private var INSTANCE: AppSettingsManager<*>? = null

        /**
         * Gets the singleton instance of [AppSettingsManager].
         *
         * @param application The main application context.
         *
         * @return The singleton instance of [AppSettingsManager].
         */
        @Suppress("UNCHECKED_CAST")
        fun <AS : IAppSettings> getInstance(
            application: Application,
            onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<AS>
        ): AppSettingsManager<AS> = INSTANCE as AppSettingsManager<AS>?
            ?: synchronized(this) {
                INSTANCE as AppSettingsManager<AS>? ?: AppSettingsManager(
                    application,
                    onAppSettingsJsonJsonReaderListener
                ).also { INSTANCE = it }
            }
    }
}
