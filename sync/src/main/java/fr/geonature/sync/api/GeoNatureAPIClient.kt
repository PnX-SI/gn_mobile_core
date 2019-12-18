package fr.geonature.sync.api

import android.content.Context
import com.google.gson.GsonBuilder
import fr.geonature.sync.api.model.AuthCredentials
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.NomenclatureType
import fr.geonature.sync.api.model.Taxref
import fr.geonature.sync.api.model.TaxrefArea
import fr.geonature.sync.api.model.User
import fr.geonature.sync.auth.AuthManager
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

/**
 * GeoNature API client.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class GeoNatureAPIClient private constructor(context: Context,
                                             baseUrl: String) {
    private val geoNatureService: GeoNatureService

    init {
        val authManager = AuthManager(context)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
            redactHeader("Cookie")
        }

        val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                // save cookie interceptor
                .addInterceptor { chain ->
                    val originalResponse = chain.proceed(chain.request())

                    originalResponse.headers("Set-Cookie")
                            .firstOrNull()
                            ?.also {
                                authManager.setCookie(it)
                            }

                    originalResponse
                }
                // set cookie interceptor
                .addInterceptor { chain ->
                    val builder = chain.request()
                            .newBuilder()

                    authManager.getCookie()
                            ?.also {
                                builder.addHeader("Cookie",
                                                  it)
                            }

                    chain.proceed(builder.build())
                }
                .build()

        val retrofit = Retrofit.Builder()
                .baseUrl("$baseUrl/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()))
                .build()

        geoNatureService = retrofit.create(GeoNatureService::class.java)
    }

    suspend fun authLogin(authCredentials: AuthCredentials): Response<AuthLogin> {
        return geoNatureService.authLogin(authCredentials)
    }

    fun sendInput(module: String,
                  input: JSONObject): Call<ResponseBody> {
        return geoNatureService.sendInput(module,
                                          RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                                                             input.toString()))
    }

    fun getMetaDatasets(): Call<ResponseBody> {
        return geoNatureService.getMetaDatasets()
    }

    fun getUsers(): Call<List<User>> {
        return geoNatureService.getUsers()
    }

    fun getTaxonomyRanks(): Call<ResponseBody> {
        return geoNatureService.getTaxonomyRanks()
    }

    fun getTaxref(): Call<List<Taxref>> {
        return geoNatureService.getTaxref()
    }

    fun getTaxrefAreas(): Call<List<TaxrefArea>> {
        return geoNatureService.getTaxrefAreas()
    }

    fun getNomenclatures(): Call<List<NomenclatureType>> {
        return geoNatureService.getNomenclatures()
    }

    fun getDefaultNomenclaturesValues(module: String): Call<ResponseBody> {
        return geoNatureService.getDefaultNomenclaturesValues(module)
    }

    companion object {

        fun instance(context: Context,
                     baseUrl: String): Lazy<GeoNatureAPIClient> = lazy {
            GeoNatureAPIClient(context,
                               baseUrl.also { if (it.endsWith('/')) it.dropLast(1) })
        }
    }
}