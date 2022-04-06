package fr.geonature.datasync.settings.io

import android.util.JsonReader
import android.util.JsonToken.BEGIN_OBJECT
import fr.geonature.commons.util.nextStringOrNull
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.settings.error.DataSyncSettingsException
import fr.geonature.datasync.settings.error.DataSyncSettingsJsonParseException
import org.tinylog.Logger
import java.io.Reader
import java.io.StringReader

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [DataSyncSettings].
 *
 * JSON entry may be an anonymous object with expected properties like:
 * ```json
 * {
 *   "geonature_url": "https://demo.geonature/geonature",
 *   "taxhub_url": "https://demo.geonature/taxhub",
 *   "gn_application_id": 3,
 *   "observers_list_id": 1,
 *   "taxa_list_id": 100,
 *   "code_area_type": "M1",
 *   "page_size": 1000,
 *   "sync_periodicity_data_essential": "20m",
 *   "sync_periodicity_data": "30m"
 * }
 * ```
 *
 * or a JSON object with `sync` property like:
 * ```json
 * {
 *   "sync": {
 *     "geonature_url": "https://demo.geonature/geonature",
 *     "taxhub_url": "https://demo.geonature/taxhub",
 *     "gn_application_id": 3,
 *     "observers_list_id": 1,
 *     "taxa_list_id": 100,
 *     "code_area_type": "M1",
 *     "page_size": 1000,
 *     "sync_periodicity_data_essential": "20m",
 *     "sync_periodicity_data": "30m"
 *   }
 * }
 * ```
 *
 * @author S. Grimault
 */
class DataSyncSettingsJsonReader {

    /**
     * parse a `JSON` string to convert as [DataSyncSettings].
     *
     * @param json the `JSON` string to parse
     *
     * @return a [DataSyncSettings] instance from the `JSON` string or `null` if something goes wrong
     *
     * @see #read(Reader)
     */
    fun read(json: String?): DataSyncSettings? {
        if (json.isNullOrBlank()) {
            return null
        }

        return runCatching { read(StringReader(json)) }
            .onFailure { exception ->
                Logger.warn(exception)
            }
            .getOrElse { null }
    }

    /**
     * parse a `JSON` reader to convert as [DataSyncSettings].
     *
     * @param reader the [Reader] to parse
     *
     * @return a [DataSyncSettings] instance from the `JSON` reader
     *
     * @throws DataSyncSettingsException if something goes wrong
     */
    fun read(reader: Reader): DataSyncSettings {
        val jsonReader = JsonReader(reader)
        val dataSyncSettings = read(jsonReader)
        jsonReader.close()

        return dataSyncSettings
    }

    /**
     * Use a [JsonReader] instance to convert as [DataSyncSettings].
     *
     * @param jsonReader the [JsonReader] to use
     *
     * @return a [DataSyncSettings] instance from [JsonReader]
     *
     * @throws DataSyncSettingsException if something goes wrong
     */
    fun read(jsonReader: JsonReader): DataSyncSettings = runCatching {
        val builder = DataSyncSettings.Builder()

        var geoNatureServerUrl: String? = null
        var taxHubServerUrl: String? = null
        var essentialDataSyncPeriodicity: String? = null
        var dataSyncPeriodicity: String? = null

        when (jsonReader.peek()) {
            BEGIN_OBJECT -> {
                jsonReader.beginObject()

                while (jsonReader.hasNext()) {
                    when (jsonReader.nextName()) {
                        "sync" -> builder.from(read(jsonReader))
                        "geonature_url" -> geoNatureServerUrl = jsonReader.nextString()
                        "taxhub_url" -> taxHubServerUrl = jsonReader.nextString()
                        "uh_application_id" -> {
                            Logger.warn { "property 'uh_application_id' is deprecated in favor of 'gn_application_id'" }
                            builder.applicationId(jsonReader.nextInt())
                        }
                        "gn_application_id" -> builder.applicationId(jsonReader.nextInt())
                        "observers_list_id" -> builder.usersListId(jsonReader.nextInt())
                        "taxa_list_id" -> builder.taxrefListId(jsonReader.nextInt())
                        "code_area_type" -> builder.codeAreaType(jsonReader.nextStringOrNull())
                        "page_size" -> builder.pageSize(jsonReader.nextInt())
                        "sync_periodicity_data_essential" -> essentialDataSyncPeriodicity =
                            jsonReader.nextStringOrNull()
                        "sync_periodicity_data" -> dataSyncPeriodicity =
                            jsonReader.nextStringOrNull()
                        else -> jsonReader.skipValue()
                    }
                }

                jsonReader.endObject()
            }
            else -> {
                jsonReader.skipValue()
            }
        }

        if (!geoNatureServerUrl.isNullOrBlank() && !taxHubServerUrl.isNullOrBlank()) {
            builder.serverUrls(
                geoNatureServerUrl = geoNatureServerUrl,
                taxHubServerUrl = taxHubServerUrl
            )
        }

        if (!dataSyncPeriodicity.isNullOrBlank() && !essentialDataSyncPeriodicity.isNullOrBlank()) {
            builder.dataSyncPeriodicity(
                dataSyncPeriodicity = dataSyncPeriodicity,
                essentialDataSyncPeriodicity = essentialDataSyncPeriodicity
            )
        }

        builder.build()
    }
        .onFailure { exception ->
            Logger.warn(exception)

            throw DataSyncSettingsJsonParseException(
                message = exception.message,
                cause = exception
            )
        }
        .getOrThrow()
}