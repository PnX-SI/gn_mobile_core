package fr.geonature.sync.api

import com.google.gson.GsonBuilder
import fr.geonature.sync.api.model.NomenclatureType
import fr.geonature.sync.api.model.Taxref
import fr.geonature.sync.api.model.TaxrefArea
import fr.geonature.sync.api.model.User
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * GeoNature API client.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class GeoNatureAPIClient private constructor(baseUrl: String) {
    private val geoNatureService: GeoNatureService

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
            redactHeader("Cookie")
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()))
            .build()

        geoNatureService = retrofit.create(GeoNatureService::class.java)
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

    companion object {

        fun instance(baseUrl: String): Lazy<GeoNatureAPIClient> = lazy { GeoNatureAPIClient(baseUrl.also { if (it.endsWith('/')) it.dropLast(1) }) }
    }
}