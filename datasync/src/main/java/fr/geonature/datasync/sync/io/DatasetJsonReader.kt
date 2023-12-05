package fr.geonature.datasync.sync.io

import android.util.JsonReader
import android.util.JsonToken
import android.util.MalformedJsonException
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.util.nextLongOrNull
import org.tinylog.Logger
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [Dataset].
 *
 * @author S. Grimault
 */
class DatasetJsonReader {

    /**
     * parse a `JSON` string to convert as list of [Dataset].
     *
     * @param json the `JSON` string to parse
     * @return a list of [Dataset] instances from the `JSON` string or empty if something goes wrong
     * @see .read
     */
    fun read(json: String?): List<Dataset> {
        if (json.isNullOrBlank()) {
            return emptyList()
        }

        try {
            return read(StringReader(json))
        } catch (e: Exception) {
            Logger.warn(e)
        }

        return emptyList()
    }

    /**
     * parse a `JSON` reader to convert as list of [Dataset].
     *
     * @param reader the `Reader` to parse
     * @return a list of [Dataset] instance from the `JSON` reader
     * @throws IOException if something goes wrong
     */
    @Throws(
        IOException::class,
        MalformedJsonException::class
    )
    fun read(reader: Reader): List<Dataset> {
        val dataset = mutableListOf<Dataset>()

        val jsonReader = JsonReader(reader)

        when (jsonReader.peek()) {
            JsonToken.BEGIN_ARRAY -> {
                dataset.addAll(readDatasetAsArray(jsonReader))
            }

            JsonToken.BEGIN_OBJECT -> {
                jsonReader.beginObject()

                while (jsonReader.hasNext()) {
                    when (jsonReader.nextName()) {
                        "data" -> dataset.addAll(readDatasetAsArray(jsonReader))
                        else -> jsonReader.skipValue()
                    }
                }

                jsonReader.endObject()
            }

            else -> {
                jsonReader.skipValue()
            }
        }

        jsonReader.close()

        return dataset
    }

    private fun readDatasetAsArray(reader: JsonReader): List<Dataset> {
        val dataset = mutableListOf<Dataset>()

        reader.beginArray()

        while (reader.hasNext()) {
            dataset.addAll(readDataset(reader))
        }

        reader.endArray()

        return dataset
    }

    private fun readDataset(reader: JsonReader): List<Dataset> {
        reader.beginObject()

        var id: Long? = null
        var name: String? = null
        var description: String? = null
        var active = false
        var createdAt: Date? = null
        val modules = mutableListOf<String>()
        var taxaListId: Long? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id_dataset" -> id = reader.nextLong()
                "dataset_name" -> name = reader.nextString()
                "dataset_desc" -> description = reader.nextString()
                "active" -> active = reader.nextBoolean()
                "meta_create_date" -> createdAt = toDate(reader.nextString())
                "modules" -> modules.addAll(readDatasetModules(reader))
                "id_taxa_list" -> taxaListId = reader
                    .nextLongOrNull()
                    ?.coerceAtLeast(0L)

                else -> reader.skipValue()
            }
        }

        reader.endObject()

        if (id == null || name.isNullOrBlank() || description.isNullOrBlank() || createdAt == null || modules.isEmpty()) {
            return emptyList()
        }

        return modules
            .asSequence()
            .distinct()
            .map {
                Dataset(
                    id,
                    it,
                    name,
                    description,
                    active,
                    createdAt,
                    taxaListId
                )
            }
            .toList()
    }

    private fun readDatasetModules(reader: JsonReader): List<String> {
        val modules = mutableListOf<String>()

        reader.beginArray()

        while (reader.hasNext()) {
            readDatasetModule(reader)?.also {
                modules.add(it)
            }
        }

        reader.endArray()

        return modules.toList()
    }

    private fun readDatasetModule(reader: JsonReader): String? {
        var module: String? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "module_path" -> module = reader
                    .nextString()
                    .lowercase(Locale.ROOT)

                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return module
    }

    internal fun toDate(str: String?): Date? {
        if (str.isNullOrBlank()) return null

        return runCatching {
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault()
            ).parse(str)
        }
            .recoverCatching {
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).parse(str)
            }
            .getOrNull()
    }
}
