package fr.geonature.sync.auth

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.commons.fp.Failure
import fr.geonature.commons.fp.getOrElse
import fr.geonature.commons.util.NetworkHandler
import fr.geonature.commons.util.add
import fr.geonature.commons.util.toIsoDateString
import fr.geonature.sync.MockitoKotlinHelper.any
import fr.geonature.sync.api.IGeoNatureAPIClient
import fr.geonature.sync.api.model.AuthCredentials
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.AuthLoginError
import fr.geonature.sync.api.model.AuthUser
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Call
import retrofit2.Response
import java.io.StringReader
import java.util.Calendar
import java.util.Date

/**
 * Unit tests about [IAuthManager].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class AuthManagerTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var authManager: IAuthManager

    @Mock
    private lateinit var geoNatureAPIClient: IGeoNatureAPIClient

    @Mock
    private lateinit var networkHandler: NetworkHandler

    @Mock
    private lateinit var authResponseCall: Call<AuthLogin>

    @Mock
    private lateinit var authLoginResponse: Response<AuthLogin>

    @Mock
    private lateinit var authLoginErrorResponse: ResponseBody

    @Mock
    private lateinit var isLoggedInObserver: Observer<Boolean>

    @Before
    fun setUp() {
        initMocks(this)

        val application = getApplicationContext<Application>()
        authManager = AuthManagerImpl(
            application,
            geoNatureAPIClient,
            networkHandler
        )
        authManager.isLoggedIn.observeForever(isLoggedInObserver)
    }

    @Test
    fun `Should return undefined AuthLogin at startup`() {
        // when reading non existing AuthLogin instance
        val noSuchAuthLogin = runBlocking { authManager.getAuthLogin() }

        // then
        assertNull(noSuchAuthLogin)
        verify(
            isLoggedInObserver,
            atLeastOnce()
        ).onChanged(false)
    }

    @Test
    fun `Should perform a successful login`() {
        // given a successful login from backend
        val authLogin = AuthLogin(
            AuthUser(
                1234,
                "Grimault",
                "Sebastien",
                2,
                8,
                "sgr"
            ),
            Date().add(
                Calendar.HOUR,
                1
            )
        )

        `when`(networkHandler.isNetworkAvailable()).thenReturn(true)
        `when`(geoNatureAPIClient.authLogin(any(AuthCredentials::class.java))).thenReturn(authResponseCall)
        `when`(authResponseCall.execute()).thenReturn(authLoginResponse)
        `when`(authLoginResponse.isSuccessful).thenReturn(true)
        `when`(authLoginResponse.body()).thenReturn(authLogin)

        verify(
            isLoggedInObserver,
            atLeastOnce()
        ).onChanged(false)

        // when perform authentication
        val auth = runBlocking {
            authManager.login(
                "sgr",
                "pass",
                2
            )
        }

        // then
        assertTrue(auth.isRight)
        assertEquals(
            authLogin,
            auth.getOrElse(null)
        )
        verify(
            isLoggedInObserver,
            atLeastOnce()
        ).onChanged(true)

        val authLoginFromManager = runBlocking { authManager.getAuthLogin() }

        assertEquals(
            authLogin.user,
            authLoginFromManager?.user
        )
        assertEquals(
            authLogin.expires.toIsoDateString(),
            authLoginFromManager?.expires?.toIsoDateString()
        )
        assertTrue(authManager.isLoggedIn.value == true)
        verify(
            isLoggedInObserver,
            atLeastOnce()
        ).onChanged(true)
    }

    @Test
    fun `Should return a network failure when no connection while trying to login`() {
        `when`(networkHandler.isNetworkAvailable()).thenReturn(false)

        // when perform authentication
        val auth = runBlocking {
            authManager.login(
                "sgr",
                "pass",
                2
            )
        }

        // then
        assertTrue(auth.isLeft)
        auth.fold({ assertTrue(it is Failure.NetworkFailure) },
            {})
        verify(
            geoNatureAPIClient,
            never()
        ).authLogin(any(AuthCredentials::class.java))
    }

    @Test
    fun `Should return AuthFailure if no successful response while trying to login`() {
        `when`(networkHandler.isNetworkAvailable()).thenReturn(true)
        `when`(geoNatureAPIClient.authLogin(any(AuthCredentials::class.java))).thenReturn(authResponseCall)
        `when`(authResponseCall.execute()).thenReturn(authLoginResponse)
        `when`(authLoginResponse.isSuccessful).thenReturn(false)
        `when`(authLoginResponse.errorBody()).thenReturn(authLoginErrorResponse)
        `when`(authLoginErrorResponse.charStream()).thenReturn(StringReader("{\"type\": \"login\", \"msg\": \"No user found with the username 'sgr' for the application with id 2\"}"))

        // when perform authentication
        val auth = runBlocking {
            authManager.login(
                "sgr",
                "pass",
                2
            )
        }

        // then
        assertTrue(auth.isLeft)
        auth.fold({
            assertEquals(
                AuthFailure(
                    AuthLoginError(
                        "login",
                        "No user found with the username 'sgr' for the application with id 2"
                    )
                ),
                it
            )
        },
            {})
    }

    @Test
    fun `Should return ServerFailure if no successful response with no body while trying to login`() {
        `when`(networkHandler.isNetworkAvailable()).thenReturn(true)
        `when`(geoNatureAPIClient.authLogin(any(AuthCredentials::class.java))).thenReturn(authResponseCall)
        `when`(authResponseCall.execute()).thenReturn(authLoginResponse)
        `when`(authLoginResponse.isSuccessful).thenReturn(false)
        `when`(authLoginResponse.errorBody()).thenReturn(authLoginErrorResponse)

        // when perform authentication
        val auth = runBlocking {
            authManager.login(
                "sgr",
                "pass",
                2
            )
        }

        // then
        assertTrue(auth.isLeft)
        auth.fold({
            assertTrue(it is Failure.ServerFailure)
        },
            {})
    }

    @Test
    fun `Should perform logout`() {
        // given a successful login from backend
        val authLogin = AuthLogin(
            AuthUser(
                1234,
                "Grimault",
                "Sebastien",
                2,
                8,
                "sgr"
            ),
            Date().add(
                Calendar.HOUR,
                1
            )
        )

        `when`(networkHandler.isNetworkAvailable()).thenReturn(true)
        `when`(geoNatureAPIClient.authLogin(any(AuthCredentials::class.java))).thenReturn(authResponseCall)
        `when`(authResponseCall.execute()).thenReturn(authLoginResponse)
        `when`(authLoginResponse.isSuccessful).thenReturn(true)
        `when`(authLoginResponse.body()).thenReturn(authLogin)

        // when perform authentication
        val auth = runBlocking {
            authManager.login(
                "sgr",
                "pass",
                2
            )
        }

        // then
        assertTrue(auth.isRight)
        assertEquals(
            authLogin,
            auth.getOrElse(null)
        )

        // when perform logout
        val disconnected = runBlocking { authManager.logout() }
        val authLoginFromManager = runBlocking { authManager.getAuthLogin() }

        // then
        verify(
            geoNatureAPIClient,
            atLeastOnce()
        ).logout()
        assertTrue(disconnected)
        assertNull(authLoginFromManager)
        verify(
            isLoggedInObserver,
            atLeastOnce()
        ).onChanged(false)
    }

    @Test
    fun `Should return no AuthLogin if the current session is expired`() {
        // given a successful login from backend
        val authLogin = AuthLogin(
            AuthUser(
                1234,
                "Grimault",
                "Sebastien",
                2,
                8,
                "sgr"
            ),
            // with an expiration date somewhere in the past
            Date().add(
                Calendar.HOUR,
                -1
            )
        )

        `when`(networkHandler.isNetworkAvailable()).thenReturn(true)
        `when`(geoNatureAPIClient.authLogin(any(AuthCredentials::class.java))).thenReturn(authResponseCall)
        `when`(authResponseCall.execute()).thenReturn(authLoginResponse)
        `when`(authLoginResponse.isSuccessful).thenReturn(true)
        `when`(authLoginResponse.body()).thenReturn(authLogin)

        // when perform authentication
        val auth = runBlocking {
            authManager.login(
                "sgr",
                "pass",
                2
            )
        }

        // then
        assertTrue(auth.isRight)
        assertEquals(
            authLogin,
            auth.getOrElse(null)
        )

        // when checking AuthLogin from manager
        val authLoginFromManager = runBlocking { authManager.getAuthLogin() }

        // then
        assertNull(authLoginFromManager)
        verify(
            isLoggedInObserver,
            atLeastOnce()
        ).onChanged(false)
    }
}
