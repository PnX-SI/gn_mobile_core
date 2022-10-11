package fr.geonature.commons.features.input.io

import android.util.JsonWriter
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.settings.IAppSettings
import org.tinylog.Logger
import java.io.IOException
import java.io.StringWriter
import java.io.Writer

/**
 * Default `JsonWriter` about writing an [AbstractInput] as `JSON`.
 *
 * @author S. Grimault
 * @see InputJsonReader
 */
class InputJsonWriter<I : AbstractInput, S : IAppSettings>(private val onInputJsonWriterListener: OnInputJsonWriterListener<I, S>) {

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
    fun setIndent(indent: String): InputJsonWriter<I, S> {
        this.indent = indent

        return this
    }

    /**
     * Convert the given [AbstractInput] as `JSON` string.
     *
     * @param input the [AbstractInput] to convert
     * @param settings additional settings
     *
     * @return a `JSON` string representation of the given [AbstractInput] or `null` if something goes wrong
     * @see .write
     */
    fun write(
        input: I?,
        settings: S? = null
    ): String? {
        if (input == null) {
            return null
        }

        val writer = StringWriter()

        try {
            write(
                writer,
                input,
                settings
            )
        } catch (ioe: IOException) {
            Logger.warn(ioe)

            return null
        }

        return writer.toString()
    }

    /**
     * Convert the given [AbstractInput] as `JSON` and write it to the given `Writer`.
     *
     * @param out the `Writer` to use
     * @param input the [AbstractInput] to convert
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    fun write(
        out: Writer,
        input: I,
        settings: S? = null
    ) {
        val writer = JsonWriter(out)
        writer.setIndent(this.indent)
        writeInput(
            writer,
            input,
            settings
        )
        writer.flush()
        writer.close()
    }

    @Throws(IOException::class)
    private fun writeInput(
        writer: JsonWriter,
        input: I,
        settings: S? = null
    ) {
        writer.beginObject()

        writer
            .name("id")
            .value(input.id)
        writer
            .name("module")
            .value(input.module)
        writer
            .name("status")
            .value(input.status.name.lowercase())

        onInputJsonWriterListener.writeAdditionalInputData(
            writer,
            input,
            settings
        )

        writer.endObject()
    }

    /**
     * Callback used by [InputJsonWriter].
     *
     * @author S. Grimault
     */
    interface OnInputJsonWriterListener<T : AbstractInput, S : IAppSettings> {

        /**
         * Adding some additional data to write from the current [AbstractInput].
         *
         * @param writer the current @code JsonWriter} to use
         * @param input the current [AbstractInput] to read
         * @param settings additional settings
         *
         * @throws IOException if something goes wrong
         */
        @Throws(IOException::class)
        fun writeAdditionalInputData(
            writer: JsonWriter,
            input: T,
            settings: S? = null
        )
    }
}
