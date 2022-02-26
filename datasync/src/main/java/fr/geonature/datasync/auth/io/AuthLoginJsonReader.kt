package fr.geonature.datasync.auth.io

import android.util.JsonReader
import fr.geonature.commons.util.toDate
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.AuthUser
import org.tinylog.Logger
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.util.Date

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [AuthLogin].
 *
 * @author S. Grimault
 *
 * @see AuthLoginJsonWriter
 */
class AuthLoginJsonReader {

    /**
     * parse a `JSON` string to convert as [AuthLogin].
     *
     * @param json the `JSON` string to parse
     * @return a [AuthLogin] instance from the `JSON` string or `null` if something goes wrong
     * @see .read
     */
    fun read(json: String?): AuthLogin? {
        if (json.isNullOrBlank()) {
            return null
        }

        try {
            return read(StringReader(json))
        } catch (ioe: IOException) {
            Logger.warn(ioe)
        }

        return null
    }

    /**
     * parse a `JSON` reader to convert as [AuthLogin].
     *
     * @param reader the `Reader` to parse
     * @return a [AuthLogin] instance from the `JSON` reader
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    fun read(reader: Reader): AuthLogin? {
        val jsonReader = JsonReader(reader)
        val authLogin = readAuthLogin(jsonReader)
        jsonReader.close()

        return authLogin
    }

    @Throws(IOException::class)
    private fun readAuthLogin(reader: JsonReader): AuthLogin? {
        var authUser: AuthUser? = null
        var expires: Date? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "user" -> authUser = readAuthUser(reader)
                "expires" -> expires = toDate(reader.nextString())
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        if (authUser == null || expires == null) {
            return null
        }

        return AuthLogin(
            authUser,
            expires
        )
    }

    @Throws(IOException::class)
    private fun readAuthUser(reader: JsonReader): AuthUser? {
        var id: Long? = null
        var lastname: String? = null
        var firstname: String? = null
        var applicationId: Int? = null
        var organismId: Int? = null
        var login: String? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextLong()
                "lastname" -> lastname = reader.nextString()
                "firstname" -> firstname = reader.nextString()
                "application_id" -> applicationId = reader.nextInt()
                "organism_id" -> organismId = reader.nextInt()
                "login" -> login = reader.nextString()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        if (id == null || lastname.isNullOrBlank() || firstname.isNullOrBlank() || applicationId == null || organismId == null || login.isNullOrBlank()) {
            return null
        }

        return AuthUser(
            id,
            lastname,
            firstname,
            applicationId,
            organismId,
            login
        )
    }
}
