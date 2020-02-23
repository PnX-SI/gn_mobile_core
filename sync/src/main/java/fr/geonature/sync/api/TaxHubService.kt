package fr.geonature.sync.api

import fr.geonature.sync.api.model.Taxref
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * TaxHub API interface definition.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
interface TaxHubService {

    @GET("api/taxref/regnewithgroupe2")
    fun getTaxonomyRanks(): Call<ResponseBody>

    // TODO: fetch all taxa
    @GET("api/taxref/allnamebylist/{id}?limit=10000")
    fun getTaxref(
        @Path("id")
        listId: Int
    ): Call<List<Taxref>>
}