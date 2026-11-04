package fr.geonature.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.geonature.api.IGeoNatureAPIClient
import fr.geonature.api.error.BaseApiException
import fr.geonature.api.error.MissingConfigurationException
import fr.geonature.api.error.NetworkException
import fr.geonature.api.model.AuthCredentials
import fr.geonature.api.model.AuthLogin
import fr.geonature.api.model.AuthLoginError
import fr.geonature.auth.error.AuthException
import fr.geonature.auth.login.io.AuthLoginJsonReader
import fr.geonature.auth.login.io.AuthLoginJsonWriter
import fr.geonature.commons.util.NetworkHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import retrofit2.Response

/**
 * Default implementation of [IAuthManager].
 *
 * @author S. Grimault
 */
class AuthManagerImpl(
    private val applicationContext: Context,
    private val geoNatureAPIClient: IGeoNatureAPIClient,
    private val networkHandler: NetworkHandler,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IAuthManager {

    private val preferenceManager: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    private val notificationManager = NotificationManagerCompat.from(applicationContext)
    private val authLoginJsonReader = AuthLoginJsonReader()
    private val authLoginJsonWriter = AuthLoginJsonWriter()

    private var authLogin: AuthLogin? = null
        set(value) {
            field = value
            _isLoggedIn.postValue(value)
        }

    private val _isLoggedIn: MutableLiveData<AuthLogin?> = MutableLiveData()
    override val isLoggedIn: LiveData<AuthLogin?> = _isLoggedIn

    override suspend fun getAuthLogin(): AuthLogin? =
        withContext(dispatcher) {
            val authLogin = this@AuthManagerImpl.authLogin

            if (authLogin != null) {
                return@withContext authLogin
            }

            val authLoginAsJson = preferenceManager.getString(
                KEY_PREFERENCE_AUTH_LOGIN,
                null
            )

            if (authLoginAsJson.isNullOrBlank()) {
                this@AuthManagerImpl.authLogin = null
                return@withContext null
            }

            authLoginJsonReader
                .read(authLoginAsJson)
                .let {
                    if (it == null) {
                        this@AuthManagerImpl.authLogin = null
                        return@let null
                    }

                    this@AuthManagerImpl.authLogin = it
                    it
                }
        }

    override suspend fun login(
        username: String,
        password: String,
        applicationId: Int,
    ): Result<AuthLogin> {
        if (!networkHandler.isNetworkAvailable()) {
            return Result.failure(NetworkException(applicationContext.getString(R.string.error_network_lost)))
        }

        // perform login from backend
        val authLoginResponse = withContext(IO) {
            runCatching {
                geoNatureAPIClient
                    .authLogin(
                        AuthCredentials(
                            username,
                            password,
                            applicationId
                        )
                    )
                    .execute()
            }.fold(onSuccess = { response: Response<AuthLogin> ->
                if (response.isSuccessful) {
                    val body = response.body()

                    if (body == null) {
                        Result.failure(BaseApiException.InternalServerException())
                    } else if (body.user.login.isBlank() || body.user.lastname.isBlank()) {
                        Logger.warn { "invalid user: ${if (body.user.login.isBlank()) "missing 'login' attribute" else "missing 'lastname' attribute"}" }
                        Result.failure(AuthException.InvalidUserException())
                    } else Result.success(body)
                } else {
                    val authLoginError = buildAuthLoginErrorResponse(response)

                    Result.failure(
                        if (authLoginError == null) BaseApiException.InternalServerException()
                        else AuthException.AuthLoginException(authLoginError)
                    )
                }
            },
                onFailure = { exception: Throwable ->
                    Result.failure(
                        when (exception) {
                            is MissingConfigurationException -> exception
                            else -> BaseApiException.InternalServerException(
                                exception.message,
                                cause = exception
                            )
                        }
                    )
                })
        }

        val authLogin = authLoginResponse.getOrNull()
            ?: return authLoginResponse

        val authLoginAsJson =
            withContext(Dispatchers.Default) { authLoginJsonWriter.write(authLogin) }

        if (authLoginAsJson.isNullOrBlank()) {
            this@AuthManagerImpl.authLogin = null
            return Result.failure(BaseApiException.InternalServerException())
        }

        Logger.info { "successfully authenticated, login expiration date: ${authLogin.expires}" }

        this.authLogin = authLogin

        notificationManager.cancel(IAuthManager.AUTH_NOTIFICATION_ID)

        return withContext(Dispatchers.Default) {
            preferenceManager
                .edit(commit = true) {
                    putString(
                        KEY_PREFERENCE_AUTH_LOGIN,
                        authLoginAsJson
                    )
                }
                .let {
                    authLoginResponse
                }
        }
    }

    override suspend fun logout() =
        withContext(dispatcher) {
            geoNatureAPIClient.logout()
            preferenceManager
                .edit()
                .remove(KEY_PREFERENCE_AUTH_LOGIN)
                .commit()
                .also {
                    if (it) {
                        authLogin = null
                    }
                }
        }

    private fun buildAuthLoginErrorResponse(response: Response<AuthLogin>): AuthLoginError? {
        val responseErrorBody = response.errorBody()
            ?: return null

        val type = object : TypeToken<AuthLoginError>() {}.type

        return runCatching {
            Gson().fromJson<AuthLoginError>(
                responseErrorBody.charStream(),
                type
            )
        }.getOrNull()
    }

    companion object {
        private const val KEY_PREFERENCE_AUTH_LOGIN = "key_preference_auth_login"
    }
}