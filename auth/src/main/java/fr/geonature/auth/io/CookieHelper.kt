package fr.geonature.auth.io

import com.google.gson.Gson
import com.google.gson.JsonParseException
import fr.geonature.api.util.hexStringToByteArray
import okhttp3.Cookie
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.ObjectInputStream

/**
 * Serialize/Deserialize Cookie.
 *
 * Cookies are serialized as JSON. The legacy Java serialization format (hexadecimal string) is
 * still readable to allow migration.
 *
 * @author S. Grimault
 */
object CookieHelper {
    private val gson = Gson()

    private data class SerializableCookie(
        val name: String?,
        val value: String?,
        val domain: String?,
        val path: String?,
        val secure: Boolean = false,
        val httpOnly: Boolean = false,
        val hostOnly: Boolean = false,
        val expiresAt: Long = -1
    )

    @JvmStatic
    @Throws(IOException::class)
    fun serialize(cookie: Cookie): String {
        return gson.toJson(
            SerializableCookie(
                name = cookie.name,
                value = cookie.value,
                domain = cookie.domain,
                path = cookie.path,
                secure = cookie.secure,
                httpOnly = cookie.httpOnly,
                hostOnly = cookie.hostOnly,
                expiresAt = if (cookie.persistent) cookie.expiresAt else -1
            )
        )
    }

    @JvmStatic
    @Throws(IOException::class)
    fun deserialize(serialized: String): Cookie {
        return if (serialized.trimStart().startsWith("{")) deserializeJson(serialized)
        else deserializeLegacy(serialized)
    }

    private fun deserializeJson(json: String): Cookie {
        try {
            val c = gson.fromJson(
                json,
                SerializableCookie::class.java
            ) ?: throw IOException("invalid cookie")
            val builder = Cookie.Builder()

            builder.name(c.name ?: throw IOException("missing cookie name"))
            builder.value(c.value ?: throw IOException("missing cookie value"))
            builder.path(c.path ?: throw IOException("missing cookie path"))

            val domain = c.domain ?: throw IOException("missing cookie domain")

            if (c.secure) builder.secure()
            if (c.httpOnly) builder.httpOnly()
            if (c.hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
            if (c.expiresAt > 0) builder.expiresAt(c.expiresAt)

            return builder.build()
        } catch (e: JsonParseException) {
            throw IOException(
                "invalid cookie",
                e
            )
        } catch (e: IllegalArgumentException) {
            throw IOException(
                "invalid cookie",
                e
            )
        }
    }

    private fun deserializeLegacy(hexString: String): Cookie {
        return ObjectInputStream(ByteArrayInputStream(hexString.hexStringToByteArray())).let {
            val builder = Cookie.Builder()
            builder.name(it.readObject() as String)
            builder.value(it.readObject() as String)

            val domain = it.readObject() as String

            builder.path(it.readObject() as String)

            if (it.readBoolean()) builder.secure()
            if (it.readBoolean()) builder.httpOnly()
            if (it.readBoolean()) builder.hostOnlyDomain(domain) else builder.domain(domain)

            it.readLong().takeIf { expiresAt -> expiresAt > 0 }?.also { expiresAt ->
                    builder.expiresAt(expiresAt)
                }

            builder.build()
        }
    }
}