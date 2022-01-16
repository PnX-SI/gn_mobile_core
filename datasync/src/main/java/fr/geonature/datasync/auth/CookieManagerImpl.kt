package fr.geonature.datasync.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import fr.geonature.datasync.auth.io.CookieHelper
import okhttp3.Cookie

/**
 * Default implementation of [ICookieManager].
 *
 * @author S. Grimault
 */
class CookieManagerImpl(applicationContext: Context) : ICookieManager {
    private val preferenceManager: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(applicationContext)

    override var cookie: Cookie? = null
        get() {
            val cookie = field

            if (cookie != null) {
                if (cookie.expiresAt < System.currentTimeMillis()) {
                    Log.i(
                        TAG,
                        "cookie expiry date ${cookie.expiresAt} reached: perform logout"
                    )

                    clearCookie()

                    return null
                }

                return cookie
            }

            return preferenceManager.getString(
                KEY_PREFERENCE_COOKIE,
                null
            )?.let { CookieHelper.deserialize(it) }
        }
        set(value) {
            field = value

            if (value == null) {
                preferenceManager.edit().remove(KEY_PREFERENCE_COOKIE).apply()

                return
            }

            preferenceManager.edit().putString(
                KEY_PREFERENCE_COOKIE,
                CookieHelper.serialize(value)
            ).apply()
        }

    override fun clearCookie() {
        cookie = null
    }

    companion object {
        private val TAG = CookieManagerImpl::class.java.name

        private const val KEY_PREFERENCE_COOKIE = "key_preference_cookie"
    }
}