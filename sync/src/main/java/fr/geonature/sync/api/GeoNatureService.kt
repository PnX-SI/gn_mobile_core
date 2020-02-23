package fr.geonature.sync.api

import fr.geonature.sync.api.model.AuthCredentials
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.NomenclatureType
import fr.geonature.sync.api.model.TaxrefArea
import fr.geonature.sync.api.model.User
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * GeoNature API interface definition.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
interface GeoNatureService {

    @POST("api/auth/login")
    suspend fun authLogin(
        @Body
        authCredentials: AuthCredentials
    ): Response<AuthLogin>

    @POST("api/{module}/releve")
    fun sendInput(
        @Path("module")
        module: String,
        @Body
        input: RequestBody
    ): Call<ResponseBody>

    @GET("api/meta/datasets")
    fun getMetaDatasets(): Call<ResponseBody>

    @GET("api/users/menu/{id}")
    fun getUsers(
        @Path("id")
        menuId: Int
    ): Call<List<User>>

    @GET("api/synthese/color_taxon")
    fun getTaxrefAreas(): Call<List<TaxrefArea>>

    @GET("api/nomenclatures/nomenclatures/taxonomy")
    fun getNomenclatures(): Call<List<NomenclatureType>>

    @GET("api/{module}/defaultNomenclatures")
    fun getDefaultNomenclaturesValues(
        @Path("module")
        module: String
    ): Call<ResponseBody>
}
