package fr.geonature.sync.api

import fr.geonature.sync.api.model.Taxref
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * TaxHub API interface definition.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
interface TaxHubService {

    @GET("api/taxref/regnewithgroupe2")
    fun getTaxonomyRanks(): Call<ResponseBody>

    @GET("api/taxref/allnamebylist/{id}")
    fun getTaxref(
        @Path("id") listId: Int,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Call<List<Taxref>>
}
