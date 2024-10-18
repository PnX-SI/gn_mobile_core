package fr.geonature.datasync.settings

import android.content.Context
import android.net.Uri
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.datasync.settings.io.DataSyncSettingsJsonReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import java.io.FileReader

/**
 * Loads [DataSyncSettings] from `URI`.
 *
 * @author S. Grimault
 */
class DataSyncSettingsUriDataSourceImpl(
    private val applicationContext: Context,
    private val resource: Uri,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IDataSyncSettingsDataSource {
    override suspend fun load(): DataSyncSettings =
        withContext(dispatcher) {
            Logger.info { "loading data sync settings from URI '${resource}'..." }

            (applicationContext.contentResolver.acquireContentProviderClient(resource)
                ?: throw DataSyncSettingsNotFoundException(source = resource.toString())).use {
                val dataSyncSettings = (runCatching {
                    it.openFile(
                        resource,
                        "r"
                    )
                }.getOrNull()
                    ?: throw DataSyncSettingsNotFoundException(source = resource.toString())).use { pfd ->
                    val dataSyncSettings = run {
                        DataSyncSettingsJsonReader().read(FileReader(pfd.fileDescriptor))
                    }

                    dataSyncSettings
                }

                dataSyncSettings
            }
        }
}