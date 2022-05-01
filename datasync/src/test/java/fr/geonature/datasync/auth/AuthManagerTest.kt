package fr.geonature.datasync.auth

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.orNull
import fr.geonature.commons.util.NetworkHandler
import fr.geonature.commons.util.add
import fr.geonature.commons.util.toIsoDateString
import fr.geonature.datasync.CoroutineTestRule
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.AuthLoginError
import fr.geonature.datasync.api.model.AuthUser
import io.mockk.Called
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
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
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AuthManagerTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var authManager: IAuthManager

    @RelaxedMockK
    private lateinit var geoNatureAPIClient: IGeoNatureAPIClient

    @MockK
    private lateinit var networkHandler: NetworkHandler

    @MockK
    private lateinit var authResponseCall: Call<AuthLogin>

    @MockK
    private lateinit var authLoginResponse: Response<AuthLogin>

    @MockK
    private lateinit var authLoginErrorResponse: ResponseBody

    @RelaxedMockK
    private lateinit var isLoggedInObserver: Observer<Boolean>

    @Before
    fun setUp() {
        init(this)

        val application = getApplicationContext<Application>()
        authManager = AuthManagerImpl(
            application,
            geoNatureAPIClient,
            networkHandler,
            coroutineTestRule.testDispatcher
        )
        authManager.isLoggedIn.observeForever(isLoggedInObserver)
    }

    @Test
    fun `should return undefined AuthLogin at startup`() = runTest {
        // when reading non existing AuthLogin instance
        val noSuchAuthLogin = authManager.getAuthLogin()

        // then
        assertNull(noSuchAuthLogin)
        verify(atLeast = 1) { isLoggedInObserver.onChanged(false) }
    }

    @Test
    fun `should perform a successful login`() = runTest {
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

        every { networkHandler.isNetworkAvailable() } returns true
        every { geoNatureAPIClient.authLogin(any()) } returns authResponseCall
        every { authResponseCall.execute() } returns authLoginResponse
        every { authLoginResponse.isSuccessful } returns true
        every { authLoginResponse.body() } returns authLogin

        verify(atLeast = 1) { isLoggedInObserver.onChanged(false) }

        // when perform authentication
        val auth = authManager.login(
            "sgr",
            "pass",
            2
        )

        // then
        assertTrue(auth.isRight)
        assertEquals(
            authLogin,
            auth.orNull()
        )
        verify(atLeast = 1) { isLoggedInObserver.onChanged(true) }

        val authLoginFromManager = authManager.getAuthLogin()

        assertEquals(
            authLogin.user,
            authLoginFromManager?.user
        )
        assertEquals(
            authLogin.expires.toIsoDateString(),
            authLoginFromManager?.expires?.toIsoDateString()
        )
        assertTrue(authManager.isLoggedIn.value == true)
        verify(atLeast = 1) { isLoggedInObserver.onChanged(true) }
    }

    @Test
    fun `should return a network failure when no connection while trying to login`() = runTest {
        every { networkHandler.isNetworkAvailable() } returns false

        // when perform authentication
        val auth = authManager.login(
            "sgr",
            "pass",
            2
        )

        // then
        assertTrue(auth.isLeft)
        auth.fold({ assertTrue(it is Failure.NetworkFailure) },
            {})
        verify { geoNatureAPIClient.authLogin(any()) wasNot Called }
    }

    @Test
    fun `should return AuthFailure if no successful response while trying to login`() = runTest {
        every { networkHandler.isNetworkAvailable() } returns true
        every { (geoNatureAPIClient.authLogin(any())) } returns authResponseCall
        every { authResponseCall.execute() } returns authLoginResponse
        every { authLoginResponse.isSuccessful } returns false
        every { authLoginResponse.errorBody() } returns authLoginErrorResponse
        every { authLoginErrorResponse.charStream() } returns StringReader("{\"type\": \"login\", \"msg\": \"No user found with the username 'sgr' for the application with id 2\"}")

        // when perform authentication
        val auth = authManager.login(
            "sgr",
            "pass",
            2
        )

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
    fun `should return ServerFailure if no successful response with no body while trying to login`() =
        runTest {
            every { networkHandler.isNetworkAvailable() } returns true
            every { geoNatureAPIClient.authLogin(any()) } returns authResponseCall
            every { authResponseCall.execute() } returns authLoginResponse
            every { authLoginResponse.isSuccessful } returns false
            every { authLoginResponse.errorBody() } returns authLoginErrorResponse

            // when perform authentication
            val auth = authManager.login(
                "sgr",
                "pass",
                2
            )

            // then
            assertTrue(auth.isLeft)
            auth.fold({
                assertTrue(it is Failure.ServerFailure)
            },
                {})
        }

    @Test
    fun `should perform logout`() = runTest {
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


        every { networkHandler.isNetworkAvailable() } returns true
        every { geoNatureAPIClient.authLogin(any()) } returns authResponseCall
        every { authResponseCall.execute() } returns authLoginResponse
        every { authLoginResponse.isSuccessful } returns true
        every { authLoginResponse.body() } returns authLogin

        // when perform authentication
        val auth = authManager.login(
            "sgr",
            "pass",
            2
        )

        // then
        assertTrue(auth.isRight)
        assertEquals(
            authLogin,
            auth.orNull()
        )

        // when perform logout
        val disconnected = authManager.logout()
        val authLoginFromManager = authManager.getAuthLogin()

        // then
        verify(atLeast = 1) { geoNatureAPIClient.logout() }
        assertTrue(disconnected)
        assertNull(authLoginFromManager)
        verify(atLeast = 1) { isLoggedInObserver.onChanged(false) }
    }

    @Test
    fun `should return no AuthLogin if the current session is expired`() = runTest {
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

        every { networkHandler.isNetworkAvailable() } returns true
        every { geoNatureAPIClient.authLogin(any()) } returns authResponseCall
        every { authResponseCall.execute() } returns authLoginResponse
        every { authLoginResponse.isSuccessful } returns true
        every { authLoginResponse.body() } returns authLogin

        // when perform authentication
        val auth = authManager.login(
            "sgr",
            "pass",
            2
        )

        // then
        assertTrue(auth.isRight)
        assertEquals(
            authLogin,
            auth.orNull()
        )

        // when checking AuthLogin from manager
        val authLoginFromManager = authManager.getAuthLogin()

        // then
        assertNull(authLoginFromManager)
        verify(atLeast = 1) { isLoggedInObserver.onChanged(false) }
    }
}
