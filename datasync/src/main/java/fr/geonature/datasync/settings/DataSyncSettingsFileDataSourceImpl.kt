package fr.geonature.datasync.settings

import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.datasync.settings.io.DataSyncSettingsJsonReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import java.io.File
import java.io.FileReader

/**
 * Loads [DataSyncSettings] from `JSON` file.
 *
 * @author S. Grimault
 */
class DataSyncSettingsFileDataSourceImpl(
    private val jsonFile: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IDataSyncSettingsDataSource {
    override suspend fun load(): DataSyncSettings = withContext(dispatcher) {
        Logger.info { "loading data sync settings from '${jsonFile.absolutePath}'..." }

        if (!jsonFile.exists()) {
            Logger.warn { "'${jsonFile.absolutePath}' not found" }

            throw DataSyncSettingsNotFoundException(source = jsonFile.absolutePath)
        }

        runCatching { DataSyncSettingsJsonReader().read(FileReader(jsonFile)) }.getOrThrow()
    }
}