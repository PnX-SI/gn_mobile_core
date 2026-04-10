package fr.geonature.datasync.auth

import android.content.Context
import android.content.SharedPreferences
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
            return cookies.firstOrNull()
        }
        set(value) {
            field = value

            if (value == null) {
                cookies = emptyList()
                return
            }

            cookies = listOf(value)
        }

    override var cookies: List<Cookie>
        get() {
            return preferenceManager
                .getString(
                    KEY_PREFERENCE_COOKIES,
                    null
                )
                ?.split(KEY_COOKIE_SEPARATOR)
                ?.mapNotNull {
                    runCatching { CookieHelper.deserialize(it) }.getOrNull()
                }
                ?: emptyList()
        }
        set(value) {
            preferenceManager
                .edit()
                .apply {
                    if (value.isEmpty()) {
                        remove(KEY_PREFERENCE_COOKIES)
                    } else {
                        putString(
                            KEY_PREFERENCE_COOKIES,
                            value.joinToString(KEY_COOKIE_SEPARATOR) { CookieHelper.serialize(it) }
                        )
                    }
                }
                .apply()
        }

    override var accessToken: String?
        get() = preferenceManager.getString(KEY_PREFERENCE_ACCESS_TOKEN, null)
        set(value) {
            preferenceManager
                .edit()
                .apply {
                    if (value.isNullOrBlank()) {
                        remove(KEY_PREFERENCE_ACCESS_TOKEN)
                    } else {
                        putString(KEY_PREFERENCE_ACCESS_TOKEN, value)
                    }
                }
                .apply()
        }

    override fun clearCookie() {
        cookie = null
        accessToken = null
    }

    companion object {
        private const val KEY_PREFERENCE_COOKIES = "key_preference_cookies"
        private const val KEY_PREFERENCE_ACCESS_TOKEN = "key_preference_access_token"
        private const val KEY_COOKIE_SEPARATOR = ","
    }
}