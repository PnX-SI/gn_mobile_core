package fr.geonature.sync.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationManagerCompat
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

    private var authLogin: AuthLogin? = null
    private var cookie: Cookie? = null

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
        val cookie = cookie

        if (cookie != null) {
            if (cookie.expiresAt() < System.currentTimeMillis()) {
                Log.i(
                    TAG,
                    "cookie expiry date ${cookie.expiresAt()} reached: perform logout"
                )

                GlobalScope.launch(Default) {
                    logout()
                }

                return null
            }

            return cookie
        }

        return preferenceManager
            .getString(
                KEY_PREFERENCE_COOKIE,
                null
            )
            ?.let { CookieHelper.deserialize(it) }
    }

    suspend fun getAuthLogin(): AuthLogin? =
        withContext(Default) {
            val authLogin = this@AuthManager.authLogin

            if (authLogin != null) {
                if (!checkSessionValidity(authLogin)) {
                    return@withContext null
                }

                return@withContext authLogin
            }

            val authLoginAsJson = preferenceManager.getString(
                KEY_PREFERENCE_AUTH_LOGIN,
                null
            )

            if (authLoginAsJson.isNullOrBlank()) {
                this@AuthManager.authLogin = null
                return@withContext null
            }

            authLoginJsonReader
                .read(authLoginAsJson)
                .let {
                    if (it == null) {
                        this@AuthManager.authLogin = null
                        return@let it
                    }

                    if (!checkSessionValidity(it)) {
                        return@let null
                    }

                    this@AuthManager.authLogin = it
                    it
                }
        }

    suspend fun setAuthLogin(authLogin: AuthLogin): Boolean {
        Log.i(
            TAG,
            "successfully authenticated, login expiration date: ${authLogin.expires}"
        )

        this.authLogin = authLogin

        return withContext(Default) {

            val authLoginAsJson = authLoginJsonWriter.write(authLogin)

            if (authLoginAsJson.isNullOrBlank()) {
                this@AuthManager.authLogin = null
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
                        authLogin = null
                        cookie = null
                    }
                }
        }

    private fun checkSessionValidity(authLogin: AuthLogin): Boolean {
        if (authLogin.expires.before(Calendar.getInstance().time)) {
            Log.i(
                TAG,
                "auth login expiry date ${authLogin.expires} reached: perform logout"
            )


            GlobalScope.launch(Default) {
                logout()
            }

            return false
        }

        return true
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
