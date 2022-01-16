package fr.geonature.sync.settings.io

import android.util.JsonReader
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import fr.geonature.datasync.settings.io.DataSyncSettingsJsonReader
import fr.geonature.sync.settings.AppSettings

/**
 * Default implementation of [AppSettingsJsonReader.OnAppSettingsJsonReaderListener].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class OnAppSettingsJsonReaderListenerImpl :
    AppSettingsJsonReader.OnAppSettingsJsonReaderListener<AppSettings> {

    override fun createAppSettings(): AppSettings {
        return AppSettings()
    }

    override fun readAdditionalAppSettingsData(
        reader: JsonReader,
        keyName: String,
        appSettings: AppSettings
    ) {
        appSettings.sync = DataSyncSettingsJsonReader().read(reader)
    }
}
