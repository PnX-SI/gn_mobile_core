package fr.geonature.datasync.auth.io

import fr.geonature.commons.util.add
import fr.geonature.datasync.util.toHex
import okhttp3.Cookie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectOutputStream
import java.util.Calendar
import java.util.Date

/**
 * Unit tests about [CookieHelper].
 *
 * @author S. Grimault
 */
class CookieHelperTest {

    @Test
    fun testSerializationDeserialization() {
        // given some existing cookie
        val cookie = Cookie
            .Builder()
            .name("token")
            .value("some_value")
            .domain("demo.geonature.fr")
            .path("/")
            .expiresAt(
                Date().add(
                    Calendar.HOUR,
                    1
                ).time
            )
            .build()

        // when serialize it
        val hexString = CookieHelper.serialize(cookie)

        // then
        assertNotNull(hexString)
        assertEquals(
            cookie,
            CookieHelper.deserialize(hexString)
        )
    }

    @Test
    fun testInvalidSerialization() {
        // given an invalid serialization
        val hexString = ByteArrayOutputStream()
            .also {
                ObjectOutputStream(it).apply {
                    writeObject("some_value")
                    writeObject("demo.geonature.fr")
                    writeObject("/")
                    writeBoolean(false)
                    writeBoolean(false)
                    writeBoolean(false)
                    writeLong(-1)
                    close()
                }
            }
            .toByteArray()
            .toHex()

        // when deserialize as cookie
        assertThrows(
            IOException::class.java
        ) { CookieHelper.deserialize(hexString) }
    }
}