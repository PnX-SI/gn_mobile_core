package fr.geonature.datasync.auth

import okhttp3.Cookie

/**
 * Manages cookie issued by GeoNature.
 *
 * @author S. Grimault
 */
interface ICookieManager {
    var cookie: Cookie?
    var cookies: List<Cookie>
    var accessToken: String?

    fun clearCookie()
}