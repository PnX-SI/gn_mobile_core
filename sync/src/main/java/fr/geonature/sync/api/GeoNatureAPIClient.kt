package fr.geonature.sync.api

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import fr.geonature.sync.api.model.AppPackage
import fr.geonature.sync.api.model.AuthCredentials
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.NomenclatureType
import fr.geonature.sync.api.model.Taxref
import fr.geonature.sync.api.model.TaxrefArea
import fr.geonature.sync.api.model.User
import fr.geonature.sync.auth.AuthManager
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
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * GeoNature API client.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class GeoNatureAPIClient private constructor(
    context: Context,
    val geoNatureBaseUrl: String,
    taxHubBaseUrl: String
) {
    private val geoNatureService: GeoNatureService
    private val taxHubService: TaxHubService

    init {
        val authManager = AuthManager.getInstance(context)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
            redactHeader("Cookie")
        }

        val client = OkHttpClient
            .Builder()
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(
                    url: HttpUrl,
                    cookies: MutableList<Cookie>
                ) {
                    cookies
                        .firstOrNull()
                        ?.also {
                            authManager.setCookie(it)
                        }
                }

                override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
                    return authManager
                        .getCookie()
                        ?.let {
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
            .addInterceptor(loggingInterceptor)
            .build()

        geoNatureService = Retrofit
            .Builder()
            .baseUrl("$geoNatureBaseUrl/")
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()
                )
            )
            .build()
            .create(GeoNatureService::class.java)

        taxHubService = Retrofit
            .Builder()
            .baseUrl("$taxHubBaseUrl/")
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setDateFormat("yyyy-MM-dd HH:mm:ss")
                        .create()
                )
            )
            .build()
            .create(TaxHubService::class.java)
    }

    suspend fun authLogin(authCredentials: AuthCredentials): Response<AuthLogin> {
        return geoNatureService.authLogin(authCredentials)
    }

    fun sendInput(
        module: String,
        input: JSONObject
    ): Call<ResponseBody> {
        return geoNatureService.sendInput(
            module,
            RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                input.toString()
            )
        )
    }

    fun getMetaDatasets(): Call<ResponseBody> {
        return geoNatureService.getMetaDatasets()
    }

    fun getUsers(menuId: Int): Call<List<User>> {
        return geoNatureService.getUsers(menuId)
    }

    fun getTaxonomyRanks(): Call<ResponseBody> {
        return taxHubService.getTaxonomyRanks()
    }

    fun getTaxref(
        listId: Int,
        limit: Int? = null,
        offset: Int? = null
    ): Call<List<Taxref>> {
        return taxHubService.getTaxref(
            listId,
            limit,
            offset
        )
    }

    fun getTaxrefAreas(
        codeAreaType: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ): Call<List<TaxrefArea>> {
        return geoNatureService.getTaxrefAreas(
            codeAreaType,
            limit,
            offset
        )
    }

    fun getNomenclatures(): Call<List<NomenclatureType>> {
        return geoNatureService.getNomenclatures()
    }

    fun getDefaultNomenclaturesValues(module: String): Call<ResponseBody> {
        return geoNatureService.getDefaultNomenclaturesValues(module)
    }

    fun getApplications(): Call<List<AppPackage>> {
        return geoNatureService.getApplications()
    }

    fun downloadPackage(url: String): Call<ResponseBody> {
        return geoNatureService.downloadPackage(url)
    }

    companion object {
        private val TAG = GeoNatureAPIClient::class.java.name

        private val baseUrl: (String?) -> String? = { url ->
            url?.also { if (it.endsWith('/')) it.dropLast(1) }
        }

        fun instance(
            context: Context,
            geoNatureBaseUrl: String? = null,
            taxHubBaseUrl: String? = null
        ): GeoNatureAPIClient? {
            val sanitizeGeoNatureBaseUrl = baseUrl(if (geoNatureBaseUrl.isNullOrBlank()) getGeoNatureServerUrl(context) else geoNatureBaseUrl)
            val sanitizeTaxHubBaseUrl = baseUrl(if (taxHubBaseUrl.isNullOrBlank()) getTaxHubServerUrl(context) else taxHubBaseUrl)

            if (sanitizeGeoNatureBaseUrl.isNullOrBlank()) {
                Log.w(
                    TAG,
                    "No GeoNature server configured"
                )

                return null
            }

            if (sanitizeTaxHubBaseUrl.isNullOrBlank()) {
                Log.w(
                    TAG,
                    "No TaxHub server configured"
                )

                return null
            }

            return GeoNatureAPIClient(
                context,
                sanitizeGeoNatureBaseUrl,
                sanitizeTaxHubBaseUrl
            )
        }
    }
}
