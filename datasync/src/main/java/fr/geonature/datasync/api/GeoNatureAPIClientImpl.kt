package fr.geonature.datasync.api

import com.google.gson.GsonBuilder
import fr.geonature.datasync.api.model.AuthCredentials
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.NomenclatureType
import fr.geonature.datasync.api.model.Taxref
import fr.geonature.datasync.api.model.TaxrefArea
import fr.geonature.datasync.api.model.User
import fr.geonature.datasync.auth.ICookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.tinylog.Logger
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * GeoNature API client.
 *
 * @author S. Grimault
 */
class GeoNatureAPIClientImpl(private val cookieManager: ICookieManager) : IGeoNatureAPIClient {

    private var geoNatureService: IGeoNatureService? = null
    private var taxHubService: ITaxHubService? = null
    private var geoNatureBaseUrl: String? = null
    private var taxHubBaseUrl: String? = null

    override fun getBaseUrls(): IGeoNatureAPIClient.ServerUrls {
        val geoNatureBaseUrl = geoNatureBaseUrl
            ?: throw IllegalStateException("missing GeoNature base URL")
        val taxHubBaseUrl = taxHubBaseUrl
            ?: throw IllegalStateException("missing TaxHub base URL")

        return IGeoNatureAPIClient.ServerUrls(
            geoNatureBaseUrl,
            taxHubBaseUrl
        )
    }

    override fun setBaseUrls(url: IGeoNatureAPIClient.ServerUrls) {
        Logger.info { "set server base URLs (GeoNature: '${url.geoNatureBaseUrl}'${if (url.taxHubBaseUrl.isNullOrBlank()) "" else ", TaxHub: '${url.taxHubBaseUrl}'"})..." }

        if (url.geoNatureBaseUrl.isNotBlank()) {
            this.geoNatureBaseUrl = url.geoNatureBaseUrl
            geoNatureService = createServiceClient(
                url.geoNatureBaseUrl,
                IGeoNatureService::class.java
            )
        }

        if (url.taxHubBaseUrl?.isNotBlank() == true) {
            this.taxHubBaseUrl = url.taxHubBaseUrl
            taxHubService = createServiceClient(
                url.taxHubBaseUrl,
                ITaxHubService::class.java
            )
        }
    }

    override fun authLogin(authCredentials: AuthCredentials): Call<AuthLogin> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.authLogin(authCredentials)
    }

    override fun logout() {
        cookieManager.clearCookie()
    }

    override fun sendInput(
        module: String,
        input: JSONObject
    ): Call<ResponseBody> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.sendInput(
            module,
            input
                .toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )
    }

    override fun getMetaDatasets(): Call<ResponseBody> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.getMetaDatasets()
    }

    override fun getUsers(menuId: Int): Call<List<User>> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.getUsers(menuId)
    }

    override fun getTaxonomyRanks(): Call<ResponseBody> {
        val taxHubService = taxHubService
            ?: throw IllegalStateException("missing TaxHub base URL")

        return taxHubService.getTaxonomyRanks()
    }

    override fun getTaxref(
        listId: Int,
        limit: Int?,
        offset: Int?
    ): Call<List<Taxref>> {
        val taxHubService = taxHubService
            ?: throw IllegalStateException("missing TaxHub base URL")

        return taxHubService.getTaxref(
            listId,
            limit,
            offset
        )
    }

    override fun getTaxrefAreas(
        codeAreaType: String?,
        limit: Int?,
        offset: Int?
    ): Call<List<TaxrefArea>> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.getTaxrefAreas(
            codeAreaType,
            limit,
            offset
        )
    }

    override fun getNomenclatures(): Call<List<NomenclatureType>> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.getNomenclatures()
    }

    override fun getDefaultNomenclaturesValues(module: String): Call<ResponseBody> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.getDefaultNomenclaturesValues(module)
    }

    override fun getApplications(): Call<ResponseBody> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.getApplications()
    }

    override fun downloadPackage(url: String): Call<ResponseBody> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.downloadPackage(url)
    }

    override fun checkSettings(): Boolean {
        return geoNatureService != null && taxHubService != null
    }

    private fun <T> createServiceClient(
        baseUrl: String,
        service: Class<T>
    ): T {
        return Retrofit
            .Builder()
            .baseUrl("${baseUrl(baseUrl)}/")
            .client(
                OkHttpClient
                    .Builder()
                    .cookieJar(object : CookieJar {
                        override fun saveFromResponse(
                            url: HttpUrl,
                            cookies: List<Cookie>
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
                    .cache(null)
                    .addInterceptor(HttpLoggingInterceptor { Logger.info { it } }.apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                        redactHeader("Authorization")
                        redactHeader("Cookie")
                    })
                    .build()
            )
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()
                )
            )
            .build()
            .create(service)
    }

    private val baseUrl: (String) -> String = { url ->
        url.also { if (it.endsWith('/')) it.dropLast(1) }
    }
}
