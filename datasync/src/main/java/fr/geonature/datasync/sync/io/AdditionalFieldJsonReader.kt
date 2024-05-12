package fr.geonature.datasync.sync.io

import android.util.JsonReader
import android.util.JsonToken
import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.data.entity.AdditionalFieldWithValues
import fr.geonature.commons.data.entity.CodeObject
import fr.geonature.commons.data.entity.FieldValue
import fr.geonature.commons.util.nextStringOrNull
import org.tinylog.Logger
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.util.Locale

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [AdditionalFieldWithValues]
 * as list.
 *
 * @author S. Grimault
 */
class AdditionalFieldJsonReader {

    /**
     * parse a `JSON` string to convert as list of [AdditionalFieldWithValues].
     *
     * @param json the `JSON` string to parse
     * @return a list of [AdditionalFieldWithValues] instances from the `JSON` string
     * @throws IOException if something goes wrong
     * @see [read][fr.geonature.occtax.features.record.io.ObservationRecordJsonReader.read(java.io.Reader)]
     */
    fun read(json: String): List<AdditionalFieldWithValues> {
        if (json.isBlank()) return emptyList()

        return read(StringReader(json))
    }

    /**
     * parse a `JSON` reader to convert as list of [AdditionalFieldWithValues].
     *
     * @param reader the `Reader` to parse
     * @return a list of [AdditionalFieldWithValues] instances from the `JSON` reader
     * @throws IOException if something goes wrong
     */
    fun read(reader: Reader): List<AdditionalFieldWithValues> {
        val jsonReader = JsonReader(reader)
        val observationRecord = readAdditionalFieldValuesAsList(jsonReader)
        jsonReader.close()

        return observationRecord
    }

    private fun readAdditionalFieldValuesAsList(reader: JsonReader): List<AdditionalFieldWithValues> {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                emptyList()
            }

            JsonToken.BEGIN_ARRAY -> {
                val additionalFieldWithValuesList = mutableListOf<AdditionalFieldWithValues>()
                reader.beginArray()
                while (reader.hasNext()) {
                    when (reader.peek()) {
                        JsonToken.BEGIN_OBJECT -> {
                            additionalFieldWithValuesList.addAll(readAdditionalFieldValues(reader))
                        }

                        else -> reader.skipValue()
                    }
                }
                reader.endArray()
                additionalFieldWithValuesList
            }

            JsonToken.BEGIN_OBJECT -> {
                readAdditionalFieldValues(reader)
            }

            else -> {
                reader.skipValue()
                emptyList()
            }
        }
    }

    private fun readAdditionalFieldValues(reader: JsonReader): List<AdditionalFieldWithValues> {
        reader.beginObject()

        var id: Long? = null
        val datasetIds = mutableListOf<Long>()
        val objects = mutableListOf<String>()
        val modules = mutableListOf<String>()
        var fieldType: AdditionalField.FieldType? = null
        var fieldName: String? = null
        var fieldLabel: String? = null
        val fieldValues = mutableListOf<Pair<String, String?>>()
        var nomenclatureType: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id_field" -> id = reader.nextLong()
                "datasets" -> datasetIds.addAll(readDatasetIds(reader))
                "objects" -> objects.addAll(readObjects(reader))
                "modules" -> modules.addAll(readModules(reader))
                "type_widget" -> fieldType = readFieldType(reader)
                "field_name" -> fieldName = reader.nextStringOrNull()
                "field_label" -> fieldLabel = reader.nextStringOrNull()
                "field_values" -> fieldValues.addAll(readFieldValues(reader))
                "code_nomenclature_type" -> nomenclatureType = reader.nextStringOrNull()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        if (id == null || fieldType == null || fieldName.isNullOrBlank() || fieldLabel.isNullOrBlank()) return emptyList()

        return modules
            .distinct()
            .map { module ->
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id,
                        fieldType,
                        fieldName,
                        fieldLabel
                    ),
                    datasetIds = datasetIds,
                    nomenclatureTypeMnemonic = nomenclatureType,
                    codeObjects = objects.map { codeObject ->
                        CodeObject(
                            id,
                            codeObject
                        )
                    },
                    values = fieldValues.map {
                        FieldValue(
                            id,
                            it.first,
                            it.second
                        )
                    },
                )
            }
    }

    private fun readDatasetIds(reader: JsonReader): List<Long> {
        val datasetIds = mutableListOf<Long>()

        reader.beginArray()

        while (reader.hasNext()) {
            readDatasetId(reader)?.also {
                datasetIds.add(it)
            }
        }

        reader.endArray()

        return datasetIds
    }

    private fun readDatasetId(reader: JsonReader): Long? {
        var datasetId: Long? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id_dataset" -> datasetId = reader.nextLong()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return datasetId
    }

    private fun readObjects(reader: JsonReader): List<String> {
        val objects = mutableListOf<String>()

        reader.beginArray()

        while (reader.hasNext()) {
            readObject(reader)?.also {
                objects.add(it)
            }
        }

        reader.endArray()

        return objects
    }

    private fun readObject(reader: JsonReader): String? {
        var codeObject: String? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "code_object" -> codeObject = reader.nextString()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return codeObject
    }

    private fun readModules(reader: JsonReader): List<String> {
        val modules = mutableListOf<String>()

        reader.beginArray()

        while (reader.hasNext()) {
            readModule(reader)?.also {
                modules.add(it)
            }
        }

        reader.endArray()

        return modules
    }

    private fun readModule(reader: JsonReader): String? {
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

    private fun readFieldType(reader: JsonReader): AdditionalField.FieldType? {
        var fieldType: AdditionalField.FieldType? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "widget_name" -> fieldType = reader
                    .nextStringOrNull()
                    ?.let { widgetName ->
                        runCatching {
                            AdditionalField.FieldType
                                .values()
                                .first { it.type == widgetName }
                        }
                            .onFailure { Logger.warn { "unknown widget '$it'" } }
                            .getOrNull()
                    }

                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return fieldType
    }

    private fun readFieldValues(reader: JsonReader): List<Pair<String, String?>> {
        return when (reader.peek()) {
            JsonToken.BEGIN_ARRAY -> {
                reader.beginArray()

                val fieldValues = mutableListOf<Pair<String, String?>>()

                while (reader.hasNext()) {
                    readFieldValue(reader)?.also {
                        fieldValues.add(it)
                    }
                }

                reader.endArray()

                fieldValues
            }

            else -> {
                reader.skipValue()
                emptyList()
            }
        }
    }

    private fun readFieldValue(reader: JsonReader): Pair<String, String?>? {
        return when (reader.peek()) {
            JsonToken.BOOLEAN -> reader
                .nextBoolean()
                .toString() to null

            JsonToken.NUMBER -> reader
                .nextLong()
                .toString() to null

            JsonToken.STRING -> reader.nextString() to null
            JsonToken.BEGIN_OBJECT -> readFieldValueAsObject(reader)
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    private fun readFieldValueAsObject(reader: JsonReader): Pair<String, String?>? {
        var value: String? = null
        var label: String? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "value" -> value = reader.nextStringOrNull()
                "label" -> label = reader.nextStringOrNull()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return value?.let { it to label }
    }
}