package fr.geonature.auth

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import fr.geonature.api.auth.ICookieManager
import fr.geonature.auth.io.CookieHelper
import okhttp3.Cookie
import java.io.IOException

/**
 * Default implementation of [ICookieManager].
 *
 * The cookie is persisted encrypted through [SecureCookieStore].
 *
 * @author S. Grimault
 */
class CookieManagerImpl(
    applicationContext: Context,
    private val secureCookieStore: SecureCookieStore = SecureCookieStore(applicationContext)
) : ICookieManager {
    private val legacyPreferences =
        PreferenceManager.getDefaultSharedPreferences(applicationContext)

    private val lock = Any()
    private var loaded = false
    private var cachedCookie: Cookie? = null

    override var cookie: Cookie?
        get() = synchronized(lock) {
            if (!loaded) {
                migrateLegacyCookie()
                cachedCookie = secureCookieStore
                    .read()
                    ?.let { runCatching { CookieHelper.deserialize(it) }.getOrNull() }
                loaded = true
            }

            cachedCookie
        }
        set(value) = synchronized(lock) {
            // legacy value must never survive, even if never read
            removeLegacyCookie()

            if (value == null) {
                secureCookieStore.clear()
            } else {
                secureCookieStore.write(CookieHelper.serialize(value))
            }

            cachedCookie = value
            loaded = true
        }

    override fun clearCookie() {
        cookie = null
    }

    private fun migrateLegacyCookie() {
        val legacy = legacyPreferences.getString(
            KEY_PREFERENCE_COOKIE,
            null
        ) ?: return

        try {
            secureCookieStore.write(CookieHelper.serialize(CookieHelper.deserialize(legacy)))
        } catch (_: IOException) {
            // unreadable legacy value: discard it
        } catch (_: ClassCastException) {
            // unreadable legacy value: discard it
        }

        removeLegacyCookie()
    }

    private fun removeLegacyCookie() {
        if (legacyPreferences.contains(KEY_PREFERENCE_COOKIE)) {
            legacyPreferences.edit { remove(KEY_PREFERENCE_COOKIE) }
        }
    }

    companion object {
        private const val KEY_PREFERENCE_COOKIE = "key_preference_cookie"
    }
}