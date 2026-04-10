package fr.geonature.datasync.auth

import android.content.Context
import android.content.SharedPreferences
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.orNull
import fr.geonature.commons.util.NetworkHandler
import fr.geonature.datasync.R
import fr.geonature.datasync.api.GeoNatureMissingConfigurationFailure
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.api.model.AuthCredentials
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.AuthLoginError
import fr.geonature.datasync.api.model.AuthMobileKeycloakRequest
import fr.geonature.datasync.auth.error.AuthFailure
import fr.geonature.datasync.auth.io.AuthLoginJsonReader
import fr.geonature.datasync.auth.io.AuthLoginJsonWriter
import fr.geonature.datasync.sync.worker.DataSyncWorker
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
    private val cookieManager: ICookieManager,
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
    ): Either<Failure, AuthLogin> {
        if (!networkHandler.isNetworkAvailable()) {
            return Either.Left(Failure.NetworkFailure(applicationContext.getString(R.string.error_network_lost)))
        }

        // perform login from backend
        val authLoginResponse = withContext(IO) {
            val authLoginResponse = runCatching {
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
                (if (response.isSuccessful) response
                    .body()
                    ?.let {
                        if (it.user.login.isBlank() || it.user.lastname.isBlank()) {
                            Logger.warn { "invalid user: ${if (it.user.login.isBlank()) "missing 'login' attribute" else "missing 'lastname' attribute"}" }
                            Either.Left(AuthFailure.InvalidUserFailure)
                        } else Either.Right(it)
                    }
                else buildAuthLoginErrorResponse(response)?.let { authLoginError ->
                    Either.Left(AuthFailure.AuthLoginFailure(authLoginError))
                })
                    ?: Either.Left<Failure>(Failure.ServerFailure)

            },
                onFailure = { exception: Throwable -> Either.Left(if (exception is IllegalArgumentException) GeoNatureMissingConfigurationFailure else Failure.ServerFailure) })

            authLoginResponse
        }

        val authLogin = authLoginResponse.orNull()
            ?: return authLoginResponse

        return persistAuthLogin(authLogin, authLoginResponse)
    }

    override suspend fun loginFromCurrentSession(): Either<Failure, AuthLogin> {
        if (!networkHandler.isNetworkAvailable()) {
            return Either.Left(Failure.NetworkFailure(applicationContext.getString(R.string.error_network_lost)))
        }

        val authLoginResponse = withContext(IO) {
            runCatching {
                geoNatureAPIClient
                    .getCurrentUser()
                    .execute()
            }.fold(
                onSuccess = { response: Response<AuthLogin> ->
                    if (response.isSuccessful) {
                        response.body()?.let { Either.Right(it) }
                    } else {
                        Either.Left(Failure.ServerFailure)
                    } ?: Either.Left(Failure.ServerFailure)
                },
                onFailure = { exception: Throwable ->
                    Either.Left(if (exception is IllegalArgumentException) GeoNatureMissingConfigurationFailure else Failure.ServerFailure)
                }
            )
        }

        val authLogin = authLoginResponse.orNull()
            ?: return authLoginResponse

        return persistAuthLogin(authLogin, authLoginResponse)
    }

    override suspend fun loginWithKeycloakCode(
        providerId: String,
        code: String,
        codeVerifier: String,
        redirectUri: String,
        applicationId: Int
    ): Either<Failure, AuthLogin> {
        if (!networkHandler.isNetworkAvailable()) {
            return Either.Left(Failure.NetworkFailure(applicationContext.getString(R.string.error_network_lost)))
        }

        val authLoginResponse = withContext(IO) {
            runCatching {
                geoNatureAPIClient
                    .authMobileKeycloakLogin(
                        AuthMobileKeycloakRequest(
                            providerId = providerId,
                            code = code,
                            codeVerifier = codeVerifier,
                            redirectUri = redirectUri,
                            applicationId = applicationId
                        )
                    )
                    .execute()
            }.fold(
                onSuccess = { response ->
                    if (response.isSuccessful) {
                        response.body()?.let { Either.Right(it) } ?: Either.Left(Failure.ServerFailure)
                    } else {
                        Either.Left(Failure.ServerFailure)
                    }
                },
                onFailure = { exception ->
                    Either.Left(if (exception is IllegalArgumentException) GeoNatureMissingConfigurationFailure else Failure.ServerFailure)
                }
            )
        }

        val authLogin = authLoginResponse.orNull()
            ?: return authLoginResponse

        return persistAuthLogin(authLogin, authLoginResponse)
    }

    override suspend fun logout() =
        withContext(dispatcher) {
            geoNatureAPIClient.logout()
            CookieManager
                .getInstance()
                .also {
                    it.removeAllCookies(null)
                    it.flush()
                }
            WebStorage.getInstance().deleteAllData()
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

    private suspend fun persistAuthLogin(
        authLogin: AuthLogin,
        authLoginResponse: Either<Failure, AuthLogin>
    ): Either<Failure, AuthLogin> {
        val authLoginAsJson =
            withContext(Dispatchers.Default) { authLoginJsonWriter.write(authLogin) }

        if (authLoginAsJson.isNullOrBlank()) {
            this@AuthManagerImpl.authLogin = null
            return Either.Left(Failure.ServerFailure)
        }

        Logger.info { "successfully authenticated, login expiration date: ${authLogin.expires}" }

        this.authLogin = authLogin
        cookieManager.accessToken = authLogin.token
        notificationManager.cancel(DataSyncWorker.AUTH_NOTIFICATION_ID)

        return withContext(Dispatchers.Default) {
            preferenceManager
                .edit()
                .putString(
                    KEY_PREFERENCE_AUTH_LOGIN,
                    authLoginAsJson
                )
                .commit()
                .let {
                    authLoginResponse
                }
        }
    }

}