package fr.geonature.datasync.settings

import android.util.Log
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.datasync.settings.io.DataSyncSettingsJsonReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader

/**
 * Loads [DataSyncSettings] from `JSON` file.
 *
 * @author S. Grimault
 */
class DataSyncSettingsFileDataSource(
    private val jsonFile: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IDataSyncSettingsDataSource {
    override suspend fun load(): DataSyncSettings = withContext(dispatcher) {
        Log.i(
            TAG,
            "loading data sync settings from '${jsonFile.absolutePath}'..."
        )

        if (!jsonFile.exists()) {
            Log.w(
                TAG,
                "'${jsonFile.absolutePath}' not found"
            )

            throw DataSyncSettingsNotFoundException(source = jsonFile.absolutePath)
        }

        runCatching { DataSyncSettingsJsonReader().read(FileReader(jsonFile)) }.getOrThrow()
    }

    companion object {
        private val TAG = DataSyncSettingsFileDataSource::class.java.name
    }
}