package fr.geonature.commons.settings

import android.app.Application
import android.util.Log
import fr.geonature.commons.model.MountPoint.StorageType.INTERNAL
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import fr.geonature.commons.util.FileUtils.getFile
import fr.geonature.commons.util.FileUtils.getRootFolder
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
class AppSettingsManager<T : IAppSettings>(internal val application: Application,
                                           onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<T>) {

    private val appSettingsJsonReader: AppSettingsJsonReader<T> = AppSettingsJsonReader(onAppSettingsJsonJsonReaderListener)

    init {
        GlobalScope.launch(Main) {
            withContext(IO) {
                getRootFolder(application,
                              INTERNAL).mkdirs()
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
    suspend fun loadAppSettings(): T? = withContext(IO) {
        val settingsJsonFile = getAppSettingsAsFile()

        Log.i(TAG,
              "Loading settings from '${settingsJsonFile.absolutePath}'...")

        if (!settingsJsonFile.exists()) {
            Log.w(TAG,
                  "'${settingsJsonFile.absolutePath}' not found")
            null
        }
        else {
            try {
                val appSettings = appSettingsJsonReader.read(FileReader(settingsJsonFile))

                Log.i(TAG,
                      "Settings loaded")

                appSettings
            }
            catch (e: IOException) {
                Log.w(TAG,
                      "Failed to load '${settingsJsonFile.name}'")

                null
            }
        }
    }

    internal fun getAppSettingsAsFile(): File {
        return getFile(getRootFolder(application,
                                     INTERNAL),
                       getAppSettingsFilename())
    }

    companion object {
        private val TAG = AppSettingsManager::class.java.name
    }
}