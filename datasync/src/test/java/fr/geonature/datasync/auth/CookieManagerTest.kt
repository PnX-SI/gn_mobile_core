package fr.geonature.datasync.auth

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.commons.util.add
import okhttp3.Cookie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar
import java.util.Date

/**
 * Unit tests about [ICookieManager].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class CookieManagerTest {

    private lateinit var cookieManager: ICookieManager

    @Before
    fun setUp() {
        val application = getApplicationContext<Application>()
        cookieManager = CookieManagerImpl(application)
    }

    @Test
    fun `should return undefined cookie at startup`() {
        // when reading non existing cookie
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
                Date().add(
                    Calendar.HOUR,
                    1
                ).time
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
    fun `should return nothing if the current cookie is expired`() {
        // given an expired Cookie to save and read
        val cookie = Cookie
            .Builder()
            .name("token")
            .value("some_value")
            .domain("demo.geonature.fr")
            .path("/")
            .expiresAt(
                Date().add(
                    Calendar.HOUR,
                    -1
                ).time
            )
            .build()

        // when setting new cookie
        cookieManager.cookie = cookie

        // when reading this cookie from manager
        val cookieFromManager = cookieManager.cookie

        // then
        assertNull(cookieFromManager)
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
                Date().add(
                    Calendar.HOUR,
                    1
                ).time
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