package fr.geonature.sync.auth

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.preference.PreferenceManager
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.auth.io.AuthLoginJsonReader
import fr.geonature.sync.auth.io.AuthLoginJsonWriter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * [AuthLogin] manager.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AuthManager(context: Context) {

    internal val preferenceManager: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private val authLoginJsonReader = AuthLoginJsonReader()
    private val authLoginJsonWriter = AuthLoginJsonWriter()

    private val _authLogin = MutableLiveData<AuthLogin?>()

    val isLoggedIn: LiveData<Boolean> = Transformations.map(_authLogin) { it != null }

    init {
        GlobalScope.launch(Main) {
            getAuthLogin()
        }
    }

    fun setCookie(cookie: String) {
        preferenceManager.edit()
            .putString(
                KEY_PREFERENCE_COOKIE,
                cookie
            )
            .apply()
    }

    fun getCookie(): String? {
        return preferenceManager.getString(
            KEY_PREFERENCE_COOKIE,
            null
        )
    }

    suspend fun getAuthLogin(): AuthLogin? = withContext(IO) {
        val authLoginAsJson = preferenceManager.getString(
            KEY_PREFERENCE_AUTH_LOGIN,
            null
        )

        if (authLoginAsJson.isNullOrBlank()) {
            _authLogin.postValue(null)
            return@withContext null
        }

        authLoginJsonReader.read(authLoginAsJson)
            .let {
                if (it?.expires?.before(Calendar.getInstance().time) == true) {
                    logout()
                    return@let null
                }

                _authLogin.postValue(it)
                it
            }
    }

    suspend fun setAuthLogin(authLogin: AuthLogin): Boolean = withContext(IO) {
        val authLoginAsJson = authLoginJsonWriter.write(authLogin)

        if (authLoginAsJson.isNullOrBlank()) {
            _authLogin.postValue(null)
            return@withContext false
        }

        _authLogin.postValue(authLogin)

        preferenceManager.edit()
            .putString(
                KEY_PREFERENCE_AUTH_LOGIN,
                authLoginAsJson
            )
            .commit()
            .also {
                _authLogin.postValue(if (it) authLogin else null)
            }
    }

    suspend fun logout(): Boolean = withContext(IO) {
        preferenceManager.edit()
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
