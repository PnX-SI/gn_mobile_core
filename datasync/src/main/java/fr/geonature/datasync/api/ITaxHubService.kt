package fr.geonature.datasync.api

import fr.geonature.datasync.api.model.TaxrefListListResult
import fr.geonature.datasync.api.model.TaxrefListResult
import fr.geonature.datasync.api.model.TaxrefVersion
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * _TaxHub_ API interface definition.
 *
 * @author S. Grimault
 */
interface ITaxHubService {

    @Headers("Accept: application/json")
    @GET("api/biblistes")
    fun getTaxrefList(): Call<TaxrefListListResult>

    @Headers("Accept: application/json")
    @GET("api/taxref/regnewithgroupe2")
    fun getTaxonomyRanks(): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("api/taxref?orderby=cd_nom&fields=listes")
    fun getTaxref(
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
        @Query("id_liste") list: String? = null
    ): Call<TaxrefListResult>

    @Headers("Accept: application/json")
    @GET("api/taxref/version")
    fun getTaxrefVersion(): Call<TaxrefVersion>
}
