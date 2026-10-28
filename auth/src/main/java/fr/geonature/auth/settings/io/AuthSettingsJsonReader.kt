package fr.geonature.auth.settings.io

import android.util.JsonReader
import android.util.JsonToken.BEGIN_OBJECT
import fr.geonature.auth.settings.domain.AuthSettings
import fr.geonature.auth.settings.error.AuthSettingsException
import org.tinylog.Logger
import java.io.Reader
import java.io.StringReader

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [AuthSettings].
 *
 * JSON entry may be an anonymous object with expected properties like:
 * ```json
 * {
 *   "gn_application_id": 3
 * }
 * ```
 *
 * or a `JSON` object with `auth` property like:
 * ```json
 * {
 *   "auth": {
 *     "gn_application_id": 3
 *   }
 * }
 * ```
 *
 * For compatibility, `gn_application_id` is also read from a `sync` property (as defined by the
 * data synchronization settings) if not defined elsewhere.
 *
 * @author S. Grimault
 */
class AuthSettingsJsonReader(private val fromExistingAuthSettings: AuthSettings? = null) {

    /**
     * parse a `JSON` string to convert as [AuthSettings].
     *
     * @param json the `JSON` string to parse
     *
     * @return an [AuthSettings] instance from the `JSON` string or `null` if something goes wrong
     *
     * @see read
     */
    fun read(json: String?): AuthSettings? {
        if (json.isNullOrBlank()) {
            return null
        }

        return runCatching { read(StringReader(json)) }
            .onFailure { exception ->
                Logger.warn(exception)
            }
            .getOrNull()
    }

    /**
     * parse a `JSON` reader to convert as [AuthSettings].
     *
     * @param reader the [Reader] to parse
     *
     * @return an [AuthSettings] instance from the `JSON` reader
     *
     * @throws AuthSettingsException if something goes wrong
     */
    fun read(reader: Reader): AuthSettings {
        return JsonReader(reader).use { read(it) }
    }

    /**
     * Use a [JsonReader] instance to convert as [AuthSettings].
     *
     * @param jsonReader the [JsonReader] to use
     *
     * @return an [AuthSettings] instance from [JsonReader]
     *
     * @throws AuthSettingsException if something goes wrong
     */
    fun read(jsonReader: JsonReader): AuthSettings =
        runCatching {
            val builder = AuthSettings
                .Builder()
                .from(fromExistingAuthSettings)

            readApplicationId(jsonReader)?.also { builder.applicationId(it) }

            builder.build()
        }
            .onFailure { exception ->
                Logger.warn(exception)

                throw AuthSettingsException.AuthSettingsJsonParseException(
                    message = exception.message,
                    cause = exception
                )
            }
            .getOrThrow()

    private fun readApplicationId(jsonReader: JsonReader): Int? {
        if (jsonReader.peek() != BEGIN_OBJECT) {
            jsonReader.skipValue()

            return null
        }

        var authApplicationId: Int? = null
        var applicationId: Int? = null
        var syncApplicationId: Int? = null

        jsonReader.beginObject()

        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "auth" -> authApplicationId = readApplicationId(jsonReader)
                "sync" -> {
                    Logger.warn { "property 'sync.gn_application_id' is deprecated in favor of 'auth.gn_application_id'" }
                    syncApplicationId = readApplicationId(jsonReader)
                }

                "uh_application_id" -> {
                    Logger.warn { "property 'uh_application_id' is deprecated in favor of 'gn_application_id'" }
                    applicationId = applicationId
                        ?: jsonReader.nextInt()
                }

                "gn_application_id" -> applicationId = jsonReader.nextInt()
                else -> jsonReader.skipValue()
            }
        }

        jsonReader.endObject()

        return authApplicationId
            ?: applicationId
            ?: syncApplicationId
    }
}
