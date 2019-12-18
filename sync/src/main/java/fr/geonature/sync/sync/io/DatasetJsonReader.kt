package fr.geonature.sync.sync.io

import android.util.JsonReader
import android.util.Log
import fr.geonature.commons.data.Dataset
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [Dataset].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
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
        }
        catch (ioe: IOException) {
            Log.w(TAG,
                  ioe.message)
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
    @Throws(IOException::class)
    fun read(reader: Reader): List<Dataset> {
        val dataset = mutableListOf<Dataset>()

        val jsonReader = JsonReader(reader)
        jsonReader.beginObject()

        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "data" -> dataset.addAll(readDatasetAsArray(jsonReader))
                else -> jsonReader.skipValue()
            }
        }

        jsonReader.endObject()

        jsonReader.close()

        return dataset
    }

    private fun readDatasetAsArray(reader: JsonReader): List<Dataset> {
        val dataset = mutableListOf<Dataset>()

        reader.beginArray()

        while (reader.hasNext()) {
            readDataset(reader)?.also {
                dataset.add(it)
            }
        }

        reader.endArray()

        return dataset
    }

    private fun readDataset(reader: JsonReader): Dataset? {
        reader.beginObject()

        var id: Long? = null
        var name: String? = null
        var description: String? = null
        var active = false
        var createdAt: Date? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id_dataset" -> id = reader.nextLong()
                "dataset_name" -> name = reader.nextString()
                "dataset_desc" -> description = reader.nextString()
                "active" -> active = reader.nextBoolean()
                "meta_create_date" -> createdAt = toDate(reader.nextString())
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        if (id == null || name.isNullOrBlank() || description.isNullOrBlank() || createdAt == null) {
            return null
        }

        return Dataset(id,
                       name,
                       description,
                       active,
                       createdAt)
    }

    internal fun toDate(str: String?): Date? {
        if (str.isNullOrBlank()) return null

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                   Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        return try {
            sdf.parse(str)
        }
        catch (pe: ParseException) {
            null
        }
    }

    companion object {
        private val TAG = DatasetJsonReader::class.java.name
    }
}