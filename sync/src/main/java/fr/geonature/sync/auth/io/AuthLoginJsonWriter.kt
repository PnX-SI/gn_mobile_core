package fr.geonature.sync.auth.io

import android.util.JsonWriter
import android.util.Log
import fr.geonature.commons.util.IsoDateUtils.toIsoDateString
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.AuthUser
import java.io.IOException
import java.io.StringWriter
import java.io.Writer

/**
 * Default `JsonWriter` about writing an [AuthLogin] as `JSON`.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see AuthLoginJsonReader
 */
class AuthLoginJsonWriter {

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
    fun setIndent(indent: String): AuthLoginJsonWriter {
        this.indent = indent

        return this
    }

    /**
     * Convert the given [AuthLogin] as `JSON` string.
     *
     * @param authLogin the [AuthLogin] to convert
     * @return a `JSON` string representation of the given [AuthLogin] or `null` if something goes wrong
     * @see .write
     */
    fun write(authLogin: AuthLogin?): String? {
        if (authLogin == null) {
            return null
        }

        val writer = StringWriter()

        try {
            write(writer,
                  authLogin)
        }
        catch (ioe: IOException) {
            Log.w(TAG,
                  ioe.message)

            return null
        }

        return writer.toString()
    }

    /**
     * Convert the given [AuthLogin] as `JSON` and write it to the given `Writer`.
     *
     * @param out   the `Writer` to use
     * @param authLogin the [AuthLogin] to convert
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    fun write(out: Writer,
              authLogin: AuthLogin) {
        val writer = JsonWriter(out)
        writer.setIndent(this.indent)
        writeAuthLogin(writer,
                       authLogin)
        writer.flush()
        writer.close()
    }

    @Throws(IOException::class)
    private fun writeAuthLogin(writer: JsonWriter,
                               authLogin: AuthLogin) {
        writer.beginObject()

        writeAuthUser(writer,
                      authLogin.user)
        writer.name("expires")
                .value(toIsoDateString(authLogin.expires))

        writer.endObject()
    }

    @Throws(IOException::class)
    private fun writeAuthUser(writer: JsonWriter,
                              authUser: AuthUser) {
        writer.name("user")
                .beginObject()

        writer.name("id")
                .value(authUser.id)
        writer.name("lastname")
                .value(authUser.lastname)
        writer.name("firstname")
                .value(authUser.firstname)
        writer.name("application_id")
                .value(authUser.applicationId)
        writer.name("organism_id")
                .value(authUser.organismId)
        writer.name("login")
                .value(authUser.login)

        writer.endObject()
    }

    companion object {
        private val TAG = AuthLoginJsonWriter::class.java.name
    }
}