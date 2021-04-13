package fr.geonature.sync.auth.io

import fr.geonature.sync.util.hexStringToByteArray
import fr.geonature.sync.util.toHex
import okhttp3.Cookie
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Serialize/Deserialize Cookie.
 *
 * @author S. Grimault
 */
object CookieHelper {

    @JvmStatic
    @Throws(IOException::class)
    fun serialize(cookie: Cookie): String {
        return ByteArrayOutputStream()
            .also {
                ObjectOutputStream(it).apply {
                    writeObject(cookie.name())
                    writeObject(cookie.value())
                    writeObject(cookie.domain())
                    writeObject(cookie.path())
                    writeBoolean(cookie.secure())
                    writeBoolean(cookie.httpOnly())
                    writeBoolean(cookie.hostOnly())
                    writeLong(if (cookie.persistent()) cookie.expiresAt() else -1)
                    close()
                }
            }
            .toByteArray()
            .toHex()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun deserialize(hexString: String): Cookie {
        return ObjectInputStream(ByteArrayInputStream(hexString.hexStringToByteArray())).let {
            val builder = Cookie.Builder()
            builder.name(it.readObject() as String)
            builder.value(it.readObject() as String)

            val domain = it.readObject() as String

            builder.path(it.readObject() as String)

            if (it.readBoolean()) builder.secure()
            if (it.readBoolean()) builder.httpOnly()
            if (it.readBoolean()) builder.hostOnlyDomain(domain) else builder.domain(domain)

            it
                .readLong()
                .takeIf { expiresAt -> expiresAt > 0 }
                ?.also { expiresAt ->
                    builder.expiresAt(expiresAt)
                }

            builder.build()
        }
    }
}