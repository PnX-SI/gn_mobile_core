package fr.geonature.commons.input.io

import android.util.JsonWriter
import android.util.Log
import fr.geonature.commons.input.AbstractInput
import java.io.IOException
import java.io.StringWriter
import java.io.Writer

/**
 * Default `JsonWriter` about writing an [AbstractInput] as `JSON`.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 * @see InputJsonReader
 */
class InputJsonWriter<T : AbstractInput>(private val onInputJsonWriterListener: OnInputJsonWriterListener<T>) {

    private var indent: String = ""

    /**
     * Sets the indentation string to be repeated for each level of indentation in the encoded document.
     * If `indent.isEmpty()` the encoded document will be compact.
     * Otherwise the encoded document will be more human-readable.
     *
     * @param indent a string containing only whitespace.
     *
     * @return InputJsonWriter fluent interface
     */
    fun setIndent(indent: String): InputJsonWriter<T> {
        this.indent = indent

        return this
    }

    /**
     * Convert the given [AbstractInput] as `JSON` string.
     *
     * @param input the [AbstractInput] to convert
     * @return a `JSON` string representation of the given [AbstractInput] or `null` if something goes wrong
     * @see .write
     */
    fun write(input: T?): String? {
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
              input: T) {
        val writer = JsonWriter(out)
        writer.setIndent(this.indent)
        writeInput(writer,
                   input)
        writer.flush()
        writer.close()
    }

    @Throws(IOException::class)
    private fun writeInput(writer: JsonWriter,
                           input: T) {
        writer.beginObject()

        writer.name("id")
            .value(input.id)
        writer.name("module")
            .value(input.module)

        onInputJsonWriterListener.writeAdditionalInputData(writer,
                                                           input)

        writer.endObject()
    }

    /**
     * Callback used by [InputJsonWriter].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    interface OnInputJsonWriterListener<T : AbstractInput> {

        /**
         * Adding some additional data to write from the current [AbstractInput].
         *
         * @param writer the current @code JsonWriter} to use
         * @param input  the current [AbstractInput] to read
         * @throws IOException if something goes wrong
         */
        @Throws(IOException::class)
        fun writeAdditionalInputData(writer: JsonWriter,
                                     input: T)
    }

    companion object {
        private val TAG = InputJsonWriter::class.java.name
    }
}
