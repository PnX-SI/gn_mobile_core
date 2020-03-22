package fr.geonature.sync.auth

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.AuthUser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

/**
 * Unit tests about [AuthManager].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class AuthManagerTest {

    private lateinit var authManager: AuthManager

    @Before
    fun setUp() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        authManager = AuthManager.getInstance(application)
        authManager.preferenceManager.edit()
            .clear()
            .commit()
    }

    @Test
    fun testGetUndefinedCookie() {
        // when reading non existing cookie
        val noSuchCookie = authManager.getCookie()

        // then
        assertNull(noSuchCookie)
    }

    @Test
    fun testSaveAndGetCookie() {
        // when setting new cookie
        authManager.setCookie("c_1234")

        // when reading this cookie from manager
        val cookie = authManager.getCookie()

        // then
        assertEquals(
            "c_1234",
            cookie
        )
    }

    @Test
    fun testGetUndefinedAuthLogin() {
        // when reading non existing AuthLogin instance
        val noSuchAuthLogin = runBlocking { authManager.getAuthLogin() }

        // then
        assertNull(noSuchAuthLogin)
    }

    @Test
    fun testSaveAndGetAuthLogin() {
        // given an AuthLogin instance to save and read
        val authLogin = AuthLogin(
            AuthUser(
                1234L,
                "Admin",
                "Test",
                3,
                1,
                "admin"
            ),
            Calendar.getInstance().apply {
                add(
                    Calendar.DAY_OF_YEAR,
                    7
                )
                set(
                    Calendar.MILLISECOND,
                    0
                )
            }.time
        )

        // when saving this AuthLogin
        val saved = runBlocking { authManager.setAuthLogin(authLogin) }

        // then
        assertTrue(saved)

        // when reading this AuthLogin from manager
        val authLoginFromManager = runBlocking { authManager.getAuthLogin() }

        // then
        assertNotNull(authLoginFromManager)
        assertEquals(
            authLogin,
            authLoginFromManager
        )
    }

    @Test
    fun testSaveAndGetExpiredAuthLogin() {
        // given an expired AuthLogin instance to save and read
        val authLogin = AuthLogin(
            AuthUser(
                1234L,
                "Admin",
                "Test",
                3,
                1,
                "admin"
            ),
            Calendar.getInstance().apply {
                add(
                    Calendar.DAY_OF_YEAR,
                    -1
                )
            }.time
        )

        // when saving this AuthLogin
        val saved = runBlocking { authManager.setAuthLogin(authLogin) }

        // then
        assertTrue(saved)

        // when reading this AuthLogin from manager
        val authLoginFromManager = runBlocking { authManager.getAuthLogin() }

        // then
        assertNull(authLoginFromManager)
    }
}
