package fr.geonature.sync.api

import android.content.Context
import com.google.gson.GsonBuilder
import fr.geonature.sync.api.model.AppPackage
import fr.geonature.sync.api.model.AuthCredentials
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.NomenclatureType
import fr.geonature.sync.api.model.Taxref
import fr.geonature.sync.api.model.TaxrefArea
import fr.geonature.sync.api.model.User
import fr.geonature.sync.auth.ICookieManager
import fr.geonature.sync.util.SettingsUtils.getGeoNatureServerUrl
import fr.geonature.sync.util.SettingsUtils.getTaxHubServerUrl
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * GeoNature API client.
 *
 * @author S. Grimault
 */
class GeoNatureAPIClientImpl(
    private val applicationContext: Context,
    private val cookieManager: ICookieManager
) : IGeoNatureAPIClient {

    private val client = OkHttpClient
        .Builder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(
                url: HttpUrl,
                cookies: MutableList<Cookie>
            ) {
                cookies
                    .firstOrNull()
                    ?.also {
                        cookieManager.cookie = it
                    }
            }

            override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
                return cookieManager.cookie?.let {
                    mutableListOf(it)
                }
                    ?: mutableListOf()
            }
        })
        .connectTimeout(
            120,
            TimeUnit.SECONDS
        )
        .readTimeout(
            120,
            TimeUnit.SECONDS
        )
        .writeTimeout(
            120,
            TimeUnit.SECONDS
        )
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
            redactHeader("Cookie")
        })
        .build()

    private val geoNatureService = {
        geoNatureBaseUrl?.let {
            Retrofit
                .Builder()
                .baseUrl("$it/")
                .client(client)
                .addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder()
                            .setDateFormat("yyyy-MM-dd HH:mm:ss")
                            .create()
                    )
                )
                .build()
                .create(IGeoNatureService::class.java)
        }
    }

    private var taxHubService = {
        taxHubBaseUrl?.let {
            Retrofit
                .Builder()
                .baseUrl("$it/")
                .client(client)
                .addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder()
                            .setDateFormat("yyyy-MM-dd HH:mm:ss")
                            .create()
                    )
                )
                .build()
                .create(ITaxHubService::class.java)
        }
    }

    override var geoNatureBaseUrl: String? = null
        get() = baseUrl(if (field.isNullOrBlank()) getGeoNatureServerUrl(applicationContext) else field)

    override var taxHubBaseUrl: String? = null
        get() = baseUrl(if (field.isNullOrBlank()) getTaxHubServerUrl(applicationContext) else field)

    override fun authLogin(authCredentials: AuthCredentials): Call<AuthLogin>? {
        return geoNatureService()?.authLogin(authCredentials)
    }

    override fun logout() {
        cookieManager.clearCookie()
    }

    override fun sendInput(
        module: String,
        input: JSONObject
    ): Call<ResponseBody>? {
        return geoNatureService()?.sendInput(
            module,
            RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                input.toString()
            )
        )
    }

    override fun getMetaDatasets(): Call<ResponseBody>? {
        return geoNatureService()?.getMetaDatasets()
    }

    override fun getUsers(menuId: Int): Call<List<User>>? {
        return geoNatureService()?.getUsers(menuId)
    }

    override fun getTaxonomyRanks(): Call<ResponseBody>? {
        return taxHubService()?.getTaxonomyRanks()
    }

    override fun getTaxref(
        listId: Int,
        limit: Int?,
        offset: Int?
    ): Call<List<Taxref>>? {
        return taxHubService()?.getTaxref(
            listId,
            limit,
            offset
        )
    }

    override fun getTaxrefAreas(
        codeAreaType: String?,
        limit: Int?,
        offset: Int?
    ): Call<List<TaxrefArea>>? {
        return geoNatureService()?.getTaxrefAreas(
            codeAreaType,
            limit,
            offset
        )
    }

    override fun getNomenclatures(): Call<List<NomenclatureType>>? {
        return geoNatureService()?.getNomenclatures()
    }

    override fun getDefaultNomenclaturesValues(module: String): Call<ResponseBody>? {
        return geoNatureService()?.getDefaultNomenclaturesValues(module)
    }

    override fun getApplications(): Call<List<AppPackage>>? {
        return geoNatureService()?.getApplications()
    }

    override fun downloadPackage(url: String): Call<ResponseBody>? {
        return geoNatureService()?.downloadPackage(url)
    }

    override fun checkSettings(): Boolean {
        return !(geoNatureBaseUrl.isNullOrBlank() || taxHubBaseUrl.isNullOrBlank())
    }

    companion object {
        private val baseUrl: (String?) -> String? = { url ->
            url?.also { if (it.endsWith('/')) it.dropLast(1) }
        }
    }
}
