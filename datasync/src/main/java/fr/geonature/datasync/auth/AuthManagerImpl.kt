package fr.geonature.datasync.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Failure
import fr.geonature.commons.fp.getOrElse
import fr.geonature.commons.util.NetworkHandler
import fr.geonature.datasync.api.GeoNatureMissingConfigurationFailure
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.api.model.AuthCredentials
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.AuthLoginError
import fr.geonature.datasync.auth.io.AuthLoginJsonReader
import fr.geonature.datasync.auth.io.AuthLoginJsonWriter
import fr.geonature.datasync.auth.worker.CheckAuthLoginWorker
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import retrofit2.Response
import java.util.Calendar

/**
 * Default implementation of [IAuthManager].
 *
 * @author S. Grimault
 */
class AuthManagerImpl(
    applicationContext: Context,
    private val geoNatureAPIClient: IGeoNatureAPIClient,
    private val networkHandler: NetworkHandler,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : IAuthManager {

    private val preferenceManager: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(applicationContext)
    private val notificationManager = NotificationManagerCompat.from(applicationContext)
    private val authLoginJsonReader = AuthLoginJsonReader()
    private val authLoginJsonWriter = AuthLoginJsonWriter()

    private var authLogin: AuthLogin? = null
        set(value) {
            field = value
            _isLoggedIn.postValue(value != null)
        }

    private val _isLoggedIn: MutableLiveData<Boolean> = MutableLiveData(false)
    override val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    override suspend fun getAuthLogin(): AuthLogin? = withContext(dispatcher) {
        val authLogin = this@AuthManagerImpl.authLogin

        if (authLogin != null) {
            if (!checkSessionValidity(authLogin)) {
                this@AuthManagerImpl.authLogin = null
                return@withContext null
            }

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
                    return@let it
                }

                if (!checkSessionValidity(it)) {
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
        applicationId: Int
    ): Either<Failure, AuthLogin> {
        if (!networkHandler.isNetworkAvailable()) {
            return Either.Left(Failure.NetworkFailure)
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
                    ?.let { Either.Right(it) }
                else buildAuthLoginErrorResponse(response)?.let { authLoginError ->
                    Either.Left(
                        AuthFailure(authLoginError)
                    )
                })
                    ?: Either.Left<Failure>(Failure.ServerFailure)

            },
                onFailure = { exception: Throwable -> Either.Left(if (exception is IllegalArgumentException) GeoNatureMissingConfigurationFailure else Failure.ServerFailure) })

            authLoginResponse
        }

        val authLogin = authLoginResponse.getOrElse(null)
            ?: return authLoginResponse

        val authLoginAsJson =
            withContext(Dispatchers.Default) { authLoginJsonWriter.write(authLogin) }

        if (authLoginAsJson.isNullOrBlank()) {
            this@AuthManagerImpl.authLogin = null
            return Either.Left<Failure>(Failure.ServerFailure)
        }

        Logger.info { "successfully authenticated, login expiration date: ${authLogin.expires}" }

        this.authLogin = authLogin

        notificationManager.cancel(CheckAuthLoginWorker.NOTIFICATION_ID)

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

    override suspend fun logout() = withContext(dispatcher) {
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

    private suspend fun checkSessionValidity(authLogin: AuthLogin): Boolean =
        withContext(dispatcher) {
            if (authLogin.expires.before(Calendar.getInstance().time)) {
                Logger.info { "auth login expiry date ${authLogin.expires} reached: perform logout" }

                logout()

                return@withContext false
            }

            return@withContext true
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