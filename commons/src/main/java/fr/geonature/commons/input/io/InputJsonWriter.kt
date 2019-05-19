package fr.geonature.commons.input.io

import android.util.JsonWriter
import android.util.Log
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.util.IsoDateUtils
import java.io.IOException
import java.io.StringWriter
import java.io.Writer

/**
 * Default `JsonWriter` about writing an [AbstractInput] as `JSON`.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 * @see InputJsonReader
 */
class InputJsonWriter(private val onInputJsonWriterListener: OnInputJsonWriterListener) {

    /**
     * Convert the given [AbstractInput] as `JSON` string.
     *
     * @param input the [AbstractInput] to convert
     * @return a `JSON` string representation of the given [AbstractInput] or `null` if something goes wrong
     * @see .write
     */
    fun write(input: AbstractInput?): String? {
        if (input == null) {
            return null
        }

        val writer = StringWriter()

        try {
            write(writer,
                  input)
        }
        catch (ioe: IOException) {
            Log.w(TAG,
                  ioe.message)

            return null
        }

        return writer.toString()
    }

    /**
     * Convert the given [AbstractInput] as `JSON` and write it to the given `Writer`.
     *
     * @param out   the `Writer` to use
     * @param input the [AbstractInput] to convert
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    fun write(out: Writer,
              input: AbstractInput) {
        val writer = JsonWriter(out)
        writeInput(writer,
                   input)
        writer.flush()
        writer.close()
    }

    @Throws(IOException::class)
    private fun writeInput(writer: JsonWriter,
                           input: AbstractInput) {
        writer.beginObject()

        writer.name("id")
            .value(input.id)
        writer.name("module")
            .value(input.module)
        writer.name("date")
            .value(IsoDateUtils.toIsoDateString(input.date))

        onInputJsonWriterListener.writeAdditionalInputData(writer,
                                                           input)

        writer.endObject()
    }

    /**
     * Callback used by [InputJsonWriter].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    interface OnInputJsonWriterListener {

        /**
         * Adding some additional data to write from the current [AbstractInput].
         *
         * @param writer the current @code JsonWriter} to use
         * @param input  the current [AbstractInput] to read
         * @throws IOException if something goes wrong
         */
        @Throws(IOException::class)
        fun writeAdditionalInputData(writer: JsonWriter,
                                     input: AbstractInput)
    }

    companion object {
        private val TAG = InputJsonWriter::class.java.name
    }
}
