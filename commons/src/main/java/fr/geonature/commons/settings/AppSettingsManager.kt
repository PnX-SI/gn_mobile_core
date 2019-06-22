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
class AppSettingsManager<T : IAppSettings>(private val application: Application,
                                           onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<T>) {

    private val appSettingsJsonReader: AppSettingsJsonReader<T> =
        AppSettingsJsonReader(onAppSettingsJsonJsonReaderListener)

    init {
        GlobalScope.launch(Main) {
            withContext(IO) {
                getRootFolder(application,
                              INTERNAL).mkdirs()
            }
        }
    }

    /**
     * Loads [IAppSettings] from `JSON` file.
     *
     * @return [IAppSettings] or `null` if not found
     */
    suspend fun loadAppSettings(): T? = withContext(IO) {
        val settingsJsonFile = getAppSettingsAsFile()

        if (!settingsJsonFile.exists()) {
            Log.w(TAG,
                  "'${settingsJsonFile.absolutePath}' not found")
            null
        }
        else {
            try {
                appSettingsJsonReader.read(FileReader(settingsJsonFile))
            }
            catch (e: IOException) {
                Log.w(TAG,
                      "Failed to load '${settingsJsonFile.name}'")

                null
            }
        }
    }

    internal fun getAppSettingsAsFile(): File {
        val packageName = application.packageName

        return getFile(getRootFolder(application,
                                     INTERNAL),
                       "settings_${packageName.substring(packageName.lastIndexOf('.') + 1)}.json")
    }

    companion object {
        private val TAG = AppSettingsManager::class.java.name
    }
}