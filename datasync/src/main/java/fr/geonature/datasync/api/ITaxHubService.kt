package fr.geonature.datasync.api

import fr.geonature.datasync.api.model.Taxref
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * TaxHub API interface definition.
 *
 * @author S. Grimault
 */
interface ITaxHubService {

    @Headers("Accept: application/json")
    @GET("api/taxref/regnewithgroupe2")
    fun getTaxonomyRanks(): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("api/taxref/allnamebylist/{id}")
    fun getTaxref(
        @Path("id") listId: Int,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Call<List<Taxref>>
}
