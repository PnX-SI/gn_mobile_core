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

    private lateinit var geoNatureService: IGeoNatureService
    private lateinit var taxHubService: ITaxHubService
    private lateinit var geoNatureBaseUrl: String
    private lateinit var taxHubBaseUrl: String
    private var isReady = false

    override fun getBaseUrls(): IGeoNatureAPIClient.ServerUrls {
        assertBaseUrlsAreDefined()

        return IGeoNatureAPIClient.ServerUrls(
            geoNatureBaseUrl,
            taxHubBaseUrl
        )
    }

    override fun setBaseUrls(
        geoNatureBaseUrl: String,
        taxHubBaseUrl: String
    ) {
        this.geoNatureBaseUrl = geoNatureBaseUrl
        this.taxHubBaseUrl = taxHubBaseUrl

        if (geoNatureBaseUrl.isBlank() || taxHubBaseUrl.isBlank()) {
            isReady = false
            return
        }

        geoNatureService = createServiceClient(
            geoNatureBaseUrl,
            IGeoNatureService::class.java
        )
        taxHubService = createServiceClient(
            taxHubBaseUrl,
            ITaxHubService::class.java
        )
        isReady = true
    }

    override fun authLogin(authCredentials: AuthCredentials): Call<AuthLogin> {
        assertBaseUrlsAreDefined()

        return geoNatureService.authLogin(authCredentials)
    }

    override fun logout() {
        cookieManager.clearCookie()
    }

    override fun sendInput(
        module: String,
        input: JSONObject
    ): Call<ResponseBody> {
        assertBaseUrlsAreDefined()

        return geoNatureService.sendInput(
            module,
            input
                .toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )
    }

    override fun getMetaDatasets(): Call<ResponseBody> {
        assertBaseUrlsAreDefined()

        return geoNatureService.getMetaDatasets()
    }

    override fun getUsers(menuId: Int): Call<List<User>> {
        assertBaseUrlsAreDefined()

        return geoNatureService.getUsers(menuId)
    }

    override fun getTaxonomyRanks(): Call<ResponseBody> {
        assertBaseUrlsAreDefined()

        return taxHubService.getTaxonomyRanks()
    }

    override fun getTaxref(
        listId: Int,
        limit: Int?,
        offset: Int?
    ): Call<List<Taxref>> {
        assertBaseUrlsAreDefined()

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
        assertBaseUrlsAreDefined()

        return geoNatureService.getTaxrefAreas(
            codeAreaType,
            limit,
            offset
        )
    }

    override fun getNomenclatures(): Call<List<NomenclatureType>> {
        assertBaseUrlsAreDefined()

        return geoNatureService.getNomenclatures()
    }

    override fun getDefaultNomenclaturesValues(module: String): Call<ResponseBody> {
        assertBaseUrlsAreDefined()

        return geoNatureService.getDefaultNomenclaturesValues(module)
    }

    override fun getApplications(): Call<ResponseBody> {
        assertBaseUrlsAreDefined()

        return geoNatureService.getApplications()
    }

    override fun downloadPackage(url: String): Call<ResponseBody> {
        assertBaseUrlsAreDefined()

        return geoNatureService.downloadPackage(url)
    }

    override fun checkSettings(): Boolean {
        return isReady
    }

    private fun assertBaseUrlsAreDefined() {
        if (!isReady) throw IllegalStateException("missing base URLs")
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
