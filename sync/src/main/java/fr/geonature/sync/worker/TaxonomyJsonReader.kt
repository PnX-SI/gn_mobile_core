package fr.geonature.sync.worker

import android.text.TextUtils
import android.util.JsonReader
import android.util.JsonToken.BEGIN_ARRAY
import android.util.JsonToken.NAME
import android.util.Log
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.util.StringUtils
import java.io.IOException
import java.io.Reader
import java.io.StringReader

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [Taxonomy].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxonomyJsonReader {

    /**
     * parse a `JSON` string to convert as list of [Taxonomy].
     *
     * @param json the `JSON` string to parse
     * @return a list of [Taxonomy] instances from the `JSON` string or empty if something goes wrong
     * @see .read
     */
    fun read(json: String?): List<Taxonomy> {
        if (StringUtils.isEmpty(json)) {
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
     * parse a `JSON` reader to convert as list of [Taxonomy].
     *
     * @param reader the `Reader` to parse
     * @return a list of [Taxonomy] instance from the `JSON` reader
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    fun read(reader: Reader): List<Taxonomy> {
        val jsonReader = JsonReader(reader)
        val input = readTaxonomyAsMap(jsonReader)
        jsonReader.close()

        return input
    }

    @Throws(IOException::class)
    private fun readTaxonomyAsMap(reader: JsonReader): List<Taxonomy> {
        val taxonomyAsMap = HashMap<String, MutableSet<String>>()
        taxonomyAsMap[Taxonomy.ANY] = mutableSetOf(Taxonomy.ANY)

        reader.beginObject()

        var kingdom: String? = null

        while (reader.hasNext()) {
            when (val jsonToken = reader.peek()) {
                NAME -> {
                    val key = reader.nextName()

                    if (TextUtils.isEmpty(key)) {
                        kingdom = null
                    }
                    else {
                        kingdom = key
                        taxonomyAsMap[kingdom!!] = mutableSetOf(Taxonomy.ANY)
                    }
                }
                BEGIN_ARRAY -> {
                    if (TextUtils.isEmpty(kingdom)) {
                        reader.skipValue()
                    }
                    else {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            val group = reader.nextString()

                            if (!TextUtils.isEmpty(group)) {
                                taxonomyAsMap[kingdom!!]?.add(group)
                            }
                        }

                        reader.endArray()
                    }
                }
                else -> {
                    reader.skipValue()
                    Log.w(TAG,
                          "Invalid object properties JSON token $jsonToken while reading taxonomy")
                }
            }
        }

        reader.endObject()

        return taxonomyAsMap.keys.asSequence()
            .map { kingdomAsKey ->
                taxonomyAsMap[kingdomAsKey]?.map { group ->
                    Taxonomy(kingdomAsKey,
                             group)
                } ?: emptyList()
            }
            .filter { it.isNotEmpty() }
            .flatMap { it.asSequence().distinct() }
            .sortedWith(Comparator { o1, o2 ->
                val kingdomCompare = o1.kingdom.compareTo(o2.kingdom)

                if (kingdomCompare != 0) {
                    when {
                        o1.kingdom == Taxonomy.ANY -> -1
                        o2.kingdom == Taxonomy.ANY -> 1
                        else -> kingdomCompare
                    }
                }
                else {
                    val groupCompare = o1.group.compareTo(o2.group)

                    if (groupCompare != 0) {
                        when {
                            o1.group == Taxonomy.ANY -> -1
                            o2.group == Taxonomy.ANY -> 1
                            else -> groupCompare
                        }
                    }
                    else {
                        groupCompare
                    }
                }
            })
            .toList()
    }

    companion object {
        private val TAG = TaxonomyJsonReader::class.java.name
    }
}