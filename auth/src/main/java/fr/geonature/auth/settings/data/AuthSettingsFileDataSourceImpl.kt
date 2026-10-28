package fr.geonature.auth.settings.data

import fr.geonature.auth.settings.domain.AuthSettings
import fr.geonature.auth.settings.error.AuthSettingsException
import fr.geonature.auth.settings.io.AuthSettingsJsonReader
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import java.io.File

/**
 * Loads [AuthSettings] from `JSON` file.
 *
 * @author S. Grimault
 */
class AuthSettingsFileDataSourceImpl(
    private val jsonFile: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IAuthSettingsDataSource {

    override suspend fun load(): AuthSettings = withContext(dispatcher) {
        Logger.info { "loading authentication settings from '${jsonFile.absolutePath}'..." }

        if (!jsonFile.exists()) {
            Logger.warn { "'${jsonFile.absolutePath}' not found" }

            throw AuthSettingsException.AuthSettingsNotFoundException(source = jsonFile.absolutePath)
        }

        jsonFile
            .reader()
            .use { AuthSettingsJsonReader().read(it) }
    }
}
