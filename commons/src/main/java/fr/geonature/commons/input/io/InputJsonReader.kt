package fr.geonature.commons.input.io

import android.util.JsonReader
import android.util.Log
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.util.StringUtils
import java.io.IOException
import java.io.Reader
import java.io.StringReader

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [AbstractInput].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see InputJsonWriter
 */
class InputJsonReader<T : AbstractInput>(private val onInputJsonReaderListener: OnInputJsonReaderListener<T>) {

    /**
     * parse a `JSON` string to convert as [AbstractInput].
     *
     * @param json the `JSON` string to parse
     * @return a [AbstractInput] instance from the `JSON` string or `null` if something goes wrong
     * @see .read
     */
    fun read(json: String?): T? {
        if (StringUtils.isEmpty(json)) {
            return null
        }

        try {
            return read(StringReader(json))
        }
        catch (ioe: IOException) {
            Log.w(TAG,
                  ioe.message)
        }

        return null
    }

    /**
     * parse a `JSON` reader to convert as [AbstractInput].
     *
     * @param reader the `Reader` to parse
     * @return a [AbstractInput] instance from the `JSON` reader
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    fun read(reader: Reader): T {
        val jsonReader = JsonReader(reader)
        val input = readInput(jsonReader)
        jsonReader.close()

        return input
    }

    @Throws(IOException::class)
    private fun readInput(reader: JsonReader): T {
        val input = onInputJsonReaderListener.createInput()

        reader.beginObject()

        while (reader.hasNext()) {
            when (val keyName = reader.nextName()) {
                "id" -> input.id = reader.nextLong()
                "module" -> input.module = reader.nextString()
                else -> onInputJsonReaderListener.readAdditionalInputData(reader,
                                                                          keyName,
                                                                          input)
            }
        }

        reader.endObject()

        return input
    }

    /**
     * Callback used by [InputJsonReader].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    interface OnInputJsonReaderListener<T : AbstractInput> {

        /**
         * Returns a new instance of [AbstractInput].
         *
         * @return new instance of [AbstractInput]
         */
        fun createInput(): T

        /**
         * Reading some additional data to set to the given [AbstractInput].
         *
         * @param reader  the current @code JsonReader} to use
         * @param keyName the JSON key read
         * @param input   the current [AbstractInput] to use
         * @throws IOException if something goes wrong
         */
        @Throws(IOException::class)
        fun readAdditionalInputData(reader: JsonReader,
                                    keyName: String,
                                    input: T)
    }

    companion object {
        private val TAG = InputJsonReader::class.java.name
    }
}
