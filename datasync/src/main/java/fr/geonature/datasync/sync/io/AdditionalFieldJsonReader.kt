package fr.geonature.datasync.sync.io

import android.util.JsonReader
import android.util.JsonToken
import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.data.entity.AdditionalFieldWithValues
import fr.geonature.commons.data.entity.CodeObject
import fr.geonature.commons.data.entity.FieldValue
import fr.geonature.commons.util.nextBooleanOrElse
import fr.geonature.commons.util.nextIntOrNull
import fr.geonature.commons.util.nextStringOrNull
import org.tinylog.Logger
import java.io.IOException
import java.io.Reader
import java.io.StringReader

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
        val additionalFieldWithValues = readAdditionalFieldValuesAsList(jsonReader)
        jsonReader.close()

        return additionalFieldWithValues
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
                            readAdditionalFieldValues(reader)?.also {
                                additionalFieldWithValuesList.add(it)
                            }
                        }

                        else -> reader.skipValue()
                    }
                }
                reader.endArray()
                additionalFieldWithValuesList
            }

            JsonToken.BEGIN_OBJECT -> {
                readAdditionalFieldValues(reader)?.let { listOf(it) }
                    ?: emptyList()
            }

            else -> {
                reader.skipValue()
                emptyList()
            }
        }
    }

    private fun readAdditionalFieldValues(reader: JsonReader): AdditionalFieldWithValues? {
        reader.beginObject()

        var id: Long? = null
        val datasetIds = mutableListOf<Long>()
        val objects = mutableListOf<String>()
        var fieldType: AdditionalField.FieldType? = null
        var fieldName: String? = null
        var fieldLabel: String? = null
        var fieldDescription: String? = null
        var fieldMandatory = false
        var fieldOrder: Int? = null
        var fieldDefaultValue: String? = null
        val fieldValues = mutableListOf<Pair<String, String?>>()
        var nomenclatureType: String? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id_field" -> id = reader.nextLong()
                "datasets" -> datasetIds.addAll(readDatasetIds(reader))
                "objects" -> objects.addAll(readObjects(reader))
                "type_widget" -> fieldType = readFieldType(reader)
                "field_name" -> fieldName = reader.nextStringOrNull()
                "field_label" -> fieldLabel = reader.nextStringOrNull()
                "description" -> fieldDescription = reader.nextStringOrNull()
                "required" -> fieldMandatory = reader.nextBooleanOrElse { false }
                "field_order" -> fieldOrder = reader.nextIntOrNull()
                "default_value" -> fieldDefaultValue = readDefaultValueAsString(reader)
                "field_values" -> fieldValues.addAll(readFieldValues(reader))
                "code_nomenclature_type" -> nomenclatureType = reader.nextStringOrNull()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        if (id == null || fieldType == null || fieldName.isNullOrBlank() || fieldLabel.isNullOrBlank()) return null

        return AdditionalFieldWithValues(
            additionalField = AdditionalField(
                id = id,
                fieldType = fieldType,
                name = fieldName,
                label = fieldLabel,
                description = fieldDescription,
                mandatory = fieldMandatory,
                order = fieldOrder,
                defaultValue = fieldDefaultValue
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

    private fun readFieldType(reader: JsonReader): AdditionalField.FieldType? {
        var fieldType: AdditionalField.FieldType? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "widget_name" -> fieldType = reader
                    .nextStringOrNull()
                    ?.let { widgetName ->
                        runCatching {
                            AdditionalField.FieldType.entries.first { it.type == widgetName }
                        }
                            .onFailure { Logger.warn { "unknown widget '$widgetName'" } }
                            .getOrNull()
                    }

                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return fieldType
    }

    private fun readDefaultValueAsString(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonToken.BOOLEAN -> reader
                .nextBoolean()
                .toString()

            JsonToken.NUMBER -> reader
                .nextLong()
                .toString()

            JsonToken.STRING -> reader.nextString()
            else -> {
                reader.skipValue()
                null
            }
        }
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
                "value" -> value = when (reader.peek()) {
                    JsonToken.BOOLEAN -> reader
                        .nextBoolean()
                        .toString()

                    JsonToken.NUMBER -> reader
                        .nextLong()
                        .toString()

                    JsonToken.STRING -> reader.nextString()
                    else -> {
                        reader.skipValue()
                        null
                    }
                }

                "label" -> label = reader.nextStringOrNull()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return value?.let { it to label }
    }
}