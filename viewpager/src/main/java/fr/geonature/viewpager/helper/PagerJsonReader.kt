package fr.geonature.viewpager.helper

import android.text.TextUtils.isEmpty
import android.util.JsonReader
import android.util.JsonToken
import android.util.Log
import fr.geonature.viewpager.model.Pager
import java.io.IOException
import java.io.Reader
import java.io.StringReader

/**
 * Default [JsonReader] about reading a `JSON` stream and build the corresponding [Pager] metadata.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see PagerJsonWriter
 */
class PagerJsonReader {

    /**
     * parse a `JSON` string to convert as [Pager].
     *
     * @param json the `JSON` string to parse
     *
     * @return a [Pager] instance from the `JSON` string or `null` if something goes wrong
     */
    fun read(json: String?): Pager? {
        if (isEmpty(json)) {
            return null
        }

        try {
            return read(StringReader(json))
        }
        catch (ioe: IOException) {
            Log.w(TAG, ioe.message)
        }

        return null
    }

    /**
     * parse a `JSON` reader to convert as [Pager].
     *
     * @param reader the [Reader] to parse
     *
     * @return a [Pager] instance from the `JSON` reader
     *
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    fun read(reader: Reader): Pager {
        val jsonReader = JsonReader(reader)
        val pager = read(jsonReader)
        jsonReader.close()

        return pager
    }

    @Throws(IOException::class)
    private fun read(reader: JsonReader): Pager {
        val pager = Pager(0)

        reader.beginObject()

        while (reader.hasNext()) {
            val keyName = reader.nextName()

            when (keyName) {
                "id" -> pager.id = reader.nextLong()
                "size" -> pager.size = reader.nextInt()
                "position" -> pager.position = reader.nextInt()
                "history" -> {
                    val jsonToken = reader.peek()

                    when (jsonToken) {
                        JsonToken.NULL -> reader.nextNull()
                        JsonToken.BEGIN_ARRAY -> {
                            reader.beginArray()

                            while (reader.hasNext()) {
                                pager.history.addLast(reader.nextInt())
                            }

                            reader.endArray()
                        }
                        else -> {
                        }
                    }
                }
            }
        }

        reader.endObject()

        return pager
    }

    companion object {

        private val TAG = PagerJsonReader::class.java.name
    }
}
