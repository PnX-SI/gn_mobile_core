package fr.geonature.viewpager.pager.io

import android.util.JsonWriter
import android.util.Log
import fr.geonature.viewpager.pager.Pager
import java.io.IOException
import java.io.StringWriter
import java.io.Writer

/**
 * Default [JsonWriter] about writing an [Pager] as `JSON`.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see PagerJsonReader
 */
class PagerJsonWriter {

    /**
     * Convert the given [Pager] as `JSON` string.
     *
     * @param pager the [Pager] to convert
     *
     * @return a `JSON` string representation of the given [Pager] or `null` if something goes wrong
     */
    fun write(pager: Pager?): String? {
        if (pager == null) {
            return null
        }

        val writer = StringWriter()

        try {
            write(writer, pager)
        }
        catch (ioe: IOException) {
            Log.w(TAG, ioe.message)

            return null
        }

        return writer.toString()
    }

    /**
     * Convert the given [Pager] as `JSON` and write it to the given `Writer`.
     *
     * @param out   the `Writer` to use
     * @param pager the [Pager] to convert
     *
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    fun write(
            out: Writer,
            pager: Pager) {
        val writer = JsonWriter(out)
        write(writer, pager)
        writer.flush()
        writer.close()
    }

    @Throws(IOException::class)
    private fun write(
            writer: JsonWriter,
            pager: Pager) {

        writer.beginObject()
        writer.name("id")
                .value(pager.id)
        writer.name("size")
                .value(pager.size.toLong())
        writer.name("position")
                .value(pager.position.toLong())

        writer.name("history")
        writer.beginArray()

        for (historyValue in pager.history) {
            writer.value(historyValue)
        }

        writer.endArray()

        writer.endObject()
    }

    companion object {

        private val TAG = PagerJsonWriter::class.java.name
    }
}
