package fr.geonature.sync.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.auth.io.AuthLoginJsonReader
import fr.geonature.sync.auth.io.AuthLoginJsonWriter
import fr.geonature.sync.auth.io.CookieHelper
import fr.geonature.sync.sync.worker.CheckAuthLoginWorker
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import java.util.Calendar

/**
 * [AuthLogin] manager.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AuthManager private constructor(applicationContext: Context) {

    internal val preferenceManager: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    private val notificationManager = NotificationManagerCompat.from(applicationContext)
    private val authLoginJsonReader = AuthLoginJsonReader()
    private val authLoginJsonWriter = AuthLoginJsonWriter()

    private val _authLogin = MutableLiveData<AuthLogin?>()
    private var cookie: Cookie? = null

    val isLoggedIn: LiveData<Boolean> = Transformations.map(_authLogin) { it != null }

    init {
        GlobalScope.launch(Default) {
            getAuthLogin()
        }
    }

    fun setCookie(cookie: Cookie) {
        this.cookie = cookie

        preferenceManager
            .edit()
            .putString(
                KEY_PREFERENCE_COOKIE,
                CookieHelper.serialize(cookie)
            )
            .apply()
    }

    fun getCookie(): Cookie? {
        return cookie
            ?: runCatching {
                preferenceManager
                    .getString(
                        KEY_PREFERENCE_COOKIE,
                        null
                    )
                    ?.let { CookieHelper.deserialize(it) }
            }.getOrNull()
    }

    suspend fun getAuthLogin(): AuthLogin? =
        withContext(Default) {
            val authLogin = _authLogin.value

            if (authLogin != null) return@withContext authLogin

            val authLoginAsJson = preferenceManager.getString(
                KEY_PREFERENCE_AUTH_LOGIN,
                null
            )

            if (authLoginAsJson.isNullOrBlank()) {
                _authLogin.postValue(null)
                return@withContext null
            }

            authLoginJsonReader
                .read(authLoginAsJson)
                .let {
                    if (it?.expires?.before(Calendar.getInstance().time) == true) {
                        Log.i(
                            TAG,
                            "auth login expiry date ${it.expires} reached: perform logout"
                        )

                        logout()
                        return@let null
                    }

                    _authLogin.postValue(it)
                    it
                }
        }

    suspend fun setAuthLogin(authLogin: AuthLogin): Boolean {
        Log.i(
            TAG,
            "successfully authenticated, login expiration date: ${authLogin.expires}"
        )

        _authLogin.value = authLogin

        return withContext(Default) {

            val authLoginAsJson = authLoginJsonWriter.write(authLogin)

            if (authLoginAsJson.isNullOrBlank()) {
                _authLogin.postValue(null)
                return@withContext false
            }

            notificationManager.cancel(CheckAuthLoginWorker.NOTIFICATION_ID)

            preferenceManager
                .edit()
                .putString(
                    KEY_PREFERENCE_AUTH_LOGIN,
                    authLoginAsJson
                )
                .commit()
        }
    }

    suspend fun logout(): Boolean =
        withContext(Default) {
            preferenceManager
                .edit()
                .remove(KEY_PREFERENCE_COOKIE)
                .remove(KEY_PREFERENCE_AUTH_LOGIN)
                .commit()
                .also {
                    if (it) {
                        _authLogin.postValue(null)
                    }
                }
        }

    companion object {
        private val TAG = AuthManager::class.java.name

        private const val KEY_PREFERENCE_COOKIE = "key_preference_cookie"
        private const val KEY_PREFERENCE_AUTH_LOGIN = "key_preference_auth_login"

        @Volatile
        private var INSTANCE: AuthManager? = null

        /** Gets the singleton instance of [AuthManager].
         *
         * @param applicationContext The main application context.
         *
         * @return The singleton instance of [AuthManager].
         */
        @Suppress("UNCHECKED_CAST")
        fun getInstance(applicationContext: Context): AuthManager =
            INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: AuthManager(applicationContext).also { INSTANCE = it }
                }
    }
}
