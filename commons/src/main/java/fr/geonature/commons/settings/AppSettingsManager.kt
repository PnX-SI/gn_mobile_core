package fr.geonature.commons.settings

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import fr.geonature.commons.data.helper.Provider
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import fr.geonature.mountpoint.model.MountPoint.StorageType.INTERNAL
import fr.geonature.mountpoint.util.FileUtils.getFile
import fr.geonature.mountpoint.util.FileUtils.getRootFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader

/**
 * Manage [IAppSettings].
 * - Read [IAppSettings] from URI
 * - Read [IAppSettings] from `JSON` file as fallback
 *
 * @author S. Grimault
 */
class AppSettingsManager<AS : IAppSettings> private constructor(
    internal val application: Application,
    onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<AS>
) {
    private val appSettingsJsonReader: AppSettingsJsonReader<AS> = AppSettingsJsonReader(onAppSettingsJsonJsonReaderListener)

    fun getAppSettingsFilename(): String {
        val packageName = application.packageName

        return "settings_${packageName.substring(packageName.lastIndexOf('.') + 1)}.json"
    }

    /**
     * Loads [IAppSettings] from URI or `JSON` file as fallback.
     *
     * @return [IAppSettings] or `null` if not found
     */
    suspend fun loadAppSettings(): AS? {
        val appSettings = withContext(Dispatchers.Default) {
            loadAppSettingsFromUri()
                ?: loadAppSettingsFromFile()
        }

        if (appSettings == null) {
            Log.w(
                TAG,
                "Failed to load '${getAppSettingsFilename()}'"
            )
        }

        return appSettings
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

    @SuppressLint("Recycle")
    private fun loadAppSettingsFromUri(): AS? {
        val appSettingsUri = Provider.buildUri(
            "settings",
            getAppSettingsFilename()
        )

        Log.i(
            TAG,
            "Loading settings from URI '${appSettingsUri}'..."
        )

        return application.contentResolver
            .acquireContentProviderClient(appSettingsUri)
            ?.let {
                val appSettings = it
                    .openFile(
                        appSettingsUri,
                        "r"
                    )
                    ?.let { pfd ->
                        val appSettings = kotlin
                            .runCatching { appSettingsJsonReader.read(FileReader(pfd.fileDescriptor)) }
                            .getOrNull()

                        if (appSettings == null) {
                            Log.w(
                                TAG,
                                "failed to load settings from URI '${appSettingsUri}'"
                            )
                        }

                        pfd.close()

                        appSettings
                    }

                it.close()

                appSettings
            }
    }

    private fun loadAppSettingsFromFile(): AS? {
        val appSettingsJsonFile = getAppSettingsAsFile()

        Log.i(
            TAG,
            "Loading settings from '${appSettingsJsonFile.absolutePath}'..."
        )

        if (!appSettingsJsonFile.exists()) {
            Log.w(
                TAG,
                "'${appSettingsJsonFile.absolutePath}' not found"
            )

            return null
        }

        return kotlin
            .runCatching { appSettingsJsonReader.read(FileReader(appSettingsJsonFile)) }
            .getOrNull()
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
        ): AppSettingsManager<AS> =
            INSTANCE as AppSettingsManager<AS>?
                ?: synchronized(this) {
                    INSTANCE as AppSettingsManager<AS>?
                        ?: AppSettingsManager(
                            application,
                            onAppSettingsJsonJsonReaderListener
                        ).also { INSTANCE = it }
                }
    }
}
