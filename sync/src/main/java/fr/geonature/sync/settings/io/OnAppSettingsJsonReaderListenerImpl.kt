package fr.geonature.sync.settings.io

import android.util.JsonReader
import fr.geonature.commons.settings.io.AppSettingsJsonReader
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
        when (keyName) {
            "geonature_url" -> appSettings.geoNatureServerUrl = reader.nextString()
            "taxhub_url" -> appSettings.taxHubServerUrl = reader.nextString()
            "uh_application_id" -> appSettings.applicationId = reader.nextInt()
            "observers_list_id" -> appSettings.usersListId = reader.nextInt()
            "taxa_list_id" -> appSettings.taxrefListId = reader.nextInt()
            "code_area_type" -> appSettings.codeAreaType = reader.nextString()
            "page_size" -> appSettings.pageSize = reader.nextInt()
            "page_max_retry" -> appSettings.pageMaxRetry = reader.nextInt()
            else -> reader.skipValue()
        }
    }
}
