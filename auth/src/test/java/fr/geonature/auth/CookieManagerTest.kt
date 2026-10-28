package fr.geonature.auth

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import fr.geonature.api.auth.ICookieManager
import fr.geonature.auth.io.CookieHelper
import okhttp3.Cookie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.Instant

/**
 * Unit tests about [ICookieManager].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class CookieManagerTest {

    private lateinit var application: Application
    private lateinit var cookieManager: ICookieManager

    private fun newStore(): SecureCookieStore {
        // Android Keystore is not available under Robolectric: use an in-memory Tink keyset
        return SecureCookieStore(application) {
            AeadConfig.register()
            KeysetHandle
                .generateNew(KeyTemplates.get("AES256_GCM"))
                .getPrimitive(
                    RegistryConfiguration.get(),
                    Aead::class.java
                )
        }
    }

    @Before
    fun setUp() {
        application = getApplicationContext()
        cookieManager = CookieManagerImpl(
            application,
            newStore()
        )
    }

    @Test
    fun `should not store the cookie in clear text`() {
        cookieManager.cookie = Cookie
            .Builder()
            .name("token")
            .value("some_secret_value")
            .domain("demo.geonature.fr")
            .path("/")
            .build()

        val stored = application
            .getSharedPreferences(
                SecureCookieStore.PREFERENCE_FILE_NAME,
                Context.MODE_PRIVATE
            )
            .all
            .values
            .joinToString()

        assertFalse(stored.contains("some_secret_value"))
    }

    @Test
    fun `should migrate legacy cookie`() {
        val cookie = Cookie
            .Builder()
            .name("token")
            .value("some_value")
            .domain("demo.geonature.fr")
            .path("/")
            .build()
        val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(application)
        defaultPreferences
            .edit()
            .putString(
                "key_preference_cookie",
                CookieHelper.serialize(cookie)
            )
            .commit()

        val migrated = CookieManagerImpl(
            application,
            newStore()
        ).cookie

        assertEquals(
            cookie,
            migrated
        )
        assertFalse(defaultPreferences.contains("key_preference_cookie"))
    }

    @Test
    fun `should return undefined cookie at startup`() {
        // when reading non-existing cookie
        val noSuchCookie = cookieManager.cookie

        // then
        assertNull(noSuchCookie)
    }

    @Test
    fun `should save and return cookie`() {
        // given a valid Cookie to save and read
        val cookie = Cookie
            .Builder()
            .name("token")
            .value("some_value")
            .domain("demo.geonature.fr")
            .path("/")
            .expiresAt(
                Instant.now().plus(Duration.ofHours(1)).toEpochMilli()
            )
            .build()

        // when setting new cookie
        cookieManager.cookie = cookie

        // when reading this cookie from manager
        val cookieFromManager = cookieManager.cookie

        // then
        assertEquals(
            cookie,
            cookieFromManager
        )
    }

    @Test
    fun `should return the current cookie even if it is expired`() {
        // given an expired Cookie to save and read
        val cookie = Cookie
            .Builder()
            .name("token")
            .value("some_value")
            .domain("demo.geonature.fr")
            .path("/")
            .expiresAt(
                Instant.now().plus(Duration.ofHours(-1)).toEpochMilli()
            )
            .build()

        // when setting new cookie
        cookieManager.cookie = cookie

        // when reading this cookie from manager
        val cookieFromManager = cookieManager.cookie

        // then
        assertEquals(
            cookie,
            cookieFromManager
        )
    }

    @Test
    fun `should return nothing if the current cookie was cleared`() {
        // given a valid Cookie to save and read
        val cookie = Cookie
            .Builder()
            .name("token")
            .value("some_value")
            .domain("demo.geonature.fr")
            .path("/")
            .expiresAt(
                Instant.now().plus(Duration.ofHours(1)).toEpochMilli()
            )
            .build()

        // when setting new cookie
        cookieManager.cookie = cookie

        // when clear cookie from manager
        cookieManager.clearCookie()

        // and reading this cookie from manager
        val cookieFromManager = cookieManager.cookie

        // then
        assertNull(cookieFromManager)
    }
}