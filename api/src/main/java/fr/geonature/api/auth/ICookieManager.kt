package fr.geonature.api.auth

import okhttp3.Cookie

/**
 * Manages cookie issued by GeoNature.
 *
 * @author S. Grimault
 */
interface ICookieManager {
    var cookie: Cookie?

    fun clearCookie()
}