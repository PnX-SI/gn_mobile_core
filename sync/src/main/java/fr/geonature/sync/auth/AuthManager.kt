package fr.geonature.sync.auth

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import fr.geonature.commons.util.StringUtils.isEmpty
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.auth.io.AuthLoginJsonReader
import fr.geonature.sync.auth.io.AuthLoginJsonWriter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

/**
 * [AuthLogin] manager.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AuthManager(context: Context) {

    internal val preferenceManager: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val authLoginJsonReader = AuthLoginJsonReader()
    private val authLoginJsonWriter = AuthLoginJsonWriter()

    fun setCookie(cookie: String) {
        preferenceManager.edit()
                .putString(KEY_PREFERENCE_COOKIE,
                           cookie)
                .apply()
    }

    fun getCookie(): String? {
        return preferenceManager.getString(KEY_PREFERENCE_COOKIE,
                                           null)
    }

    suspend fun getAuthLogin(): AuthLogin? = withContext(IO) {
        val authLoginAsJson = preferenceManager.getString(KEY_PREFERENCE_AUTH_LOGIN,
                                                          null)

        if (isEmpty(authLoginAsJson)) {
            return@withContext null
        }

        authLoginJsonReader.read(authLoginAsJson)
    }

    suspend fun setAuthLogin(authLogin: AuthLogin): Boolean = withContext(IO) {
        val authLoginAsJson = authLoginJsonWriter.write(authLogin)

        if (isEmpty(authLoginAsJson)) return@withContext false

        preferenceManager.edit()
                .putString(KEY_PREFERENCE_AUTH_LOGIN,
                           authLoginAsJson)
                .commit()
    }

    companion object {
        private const val KEY_PREFERENCE_COOKIE = "key_preference_cookie"
        private const val KEY_PREFERENCE_AUTH_LOGIN = "key_preference_auth_login"

        @Volatile
        private var INSTANCE: AuthManager? = null

        /** Gets the singleton instance of [AuthManager].
         *
         * @param application The main context context.
         *
         * @return The singleton instance of [AuthManager].
         */
        @Suppress("UNCHECKED_CAST")
        fun getInstance(application: Application): AuthManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: AuthManager(application).also { INSTANCE = it }
        }
    }
}