package fr.geonature.datasync.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.datasync.settings.io.DataSyncSettingsJsonReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileReader

/**
 * Loads [DataSyncSettings] from `URI`.
 *
 * @author S. Grimault
 */
class DataSyncSettingsUriDataSource(
    private val applicationContext: Context,
    private val resource: Uri,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IDataSyncSettingsDataSource {
    override suspend fun load(): DataSyncSettings = withContext(dispatcher) {
        Log.i(
            TAG,
            "loading data sync settings from URI '${resource}'..."
        )

        (applicationContext.contentResolver.acquireContentProviderClient(resource)
            ?: throw DataSyncSettingsNotFoundException(source = resource.toString())).use {
            val dataSyncSettings = (runCatching {
                it.openFile(
                    resource,
                    "r"
                )
            }.getOrNull()
                ?: throw DataSyncSettingsNotFoundException(source = resource.toString())).use { pfd ->
                val dataSyncSettings =
                    runCatching { DataSyncSettingsJsonReader().read(FileReader(pfd.fileDescriptor)) }.getOrThrow()

                dataSyncSettings
            }

            dataSyncSettings
        }
    }

    companion object {
        private val TAG = DataSyncSettingsUriDataSource::class.java.name
    }
}