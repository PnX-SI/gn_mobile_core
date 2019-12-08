package fr.geonature.sync.api

import fr.geonature.sync.api.model.AuthCredentials
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.NomenclatureType
import fr.geonature.sync.api.model.Taxref
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

    @POST("geonature/api/auth/login")
    suspend fun authLogin(@Body
                          authCredentials: AuthCredentials): Response<AuthLogin>

    @POST("geonature/api/{module}/releve")
    suspend fun sendInput(@Path("module")
                          module: String,
                          @Body
                          input: RequestBody): Call<ResponseBody>

    @GET("geonature/api/meta/datasets")
    fun getMetaDatasets(): Call<ResponseBody>

    @GET("geonature/api/users/menu/1")
    fun getUsers(): Call<List<User>>

    @GET("taxhub/api/taxref/regnewithgroupe2")
    fun getTaxonomyRanks(): Call<ResponseBody>

    @GET("taxhub/api/taxref/allnamebylist/100")
    fun getTaxref(): Call<List<Taxref>>

    @GET("geonature/api/synthese/color_taxon")
    fun getTaxrefAreas(): Call<List<TaxrefArea>>

    @GET("geonature/api/nomenclatures/nomenclatures/taxonomy")
    fun getNomenclatures(): Call<List<NomenclatureType>>

    @GET("geonature/api/{module}/defaultNomenclatures")
    fun getDefaultNomenclaturesValues(@Path("module")
                                      module: String): Call<ResponseBody>
}