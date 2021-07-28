package fr.geonature.sync.api

import fr.geonature.sync.api.model.AppPackage
import fr.geonature.sync.api.model.AuthCredentials
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.NomenclatureType
import fr.geonature.sync.api.model.TaxrefArea
import fr.geonature.sync.api.model.User
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * GeoNature API interface definition.
 *
 * @author S. Grimault
 */
interface IGeoNatureService {

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json;charset=UTF-8"
    )
    @POST("api/auth/login")
    fun authLogin(
        @Body authCredentials: AuthCredentials
    ): Call<AuthLogin>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json;charset=UTF-8"
    )
    @POST("api/{module}/releve")
    fun sendInput(
        @Path("module") module: String,
        @Body input: RequestBody
    ): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("api/meta/datasets?fields=modules")
    fun getMetaDatasets(): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("api/users/menu/{id}")
    fun getUsers(
        @Path("id") menuId: Int
    ): Call<List<User>>

    @Headers("Accept: application/json")
    @GET("api/synthese/color_taxon")
    fun getTaxrefAreas(
        @Query("code_area_type") codeAreaType: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Call<List<TaxrefArea>>

    @Headers("Accept: application/json")
    @GET("api/nomenclatures/nomenclatures/taxonomy")
    fun getNomenclatures(): Call<List<NomenclatureType>>

    @Headers("Accept: application/json")
    @GET("api/{module}/defaultNomenclatures")
    fun getDefaultNomenclaturesValues(
        @Path("module") module: String
    ): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("api/gn_commons/t_mobile_apps")
    fun getApplications(): Call<List<AppPackage>>

    @Streaming
    @GET
    fun downloadPackage(
        @Url url: String
    ): Call<ResponseBody>
}
