package fr.geonature.datasync.api

import fr.geonature.datasync.api.model.AuthCredentials
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.DatasetQuery
import fr.geonature.datasync.api.model.Media
import fr.geonature.datasync.api.model.NomenclatureType
import fr.geonature.datasync.api.model.TaxrefArea
import fr.geonature.datasync.api.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * _GeoNature_ API interface definition.
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
        "Accept: application/json, text/plain, */*"
    )
    @POST("api/gn_commons/media")
    @Multipart
    fun sendMediaFile(
        @Part("id_nomenclature_media_type") mediaType: Long,
        @Part("id_table_location") tableLocation: Long,
        @Part("author") author: RequestBody,
        @Part("title_en") titleEn: RequestBody?,
        @Part("title_fr") titleFr: RequestBody?,
        @Part("description_en") descriptionEn: RequestBody?,
        @Part("description_fr") descriptionFr: RequestBody?,
        @Part mediaFile: MultipartBody.Part
    ): Call<Media>

    @DELETE("api/gn_commons/media/{id}")
    fun deleteMediaFile(@Path("id") mediaId: Int): Call<ResponseBody>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json;charset=UTF-8"
    )
    @POST("api/meta/datasets")
    fun getMetaDatasets(@Body query: DatasetQuery): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("api/users/menu/{id}")
    fun getUsers(
        @Path("id") menuId: Int
    ): Call<List<User>>

    @Headers("Accept: application/json")
    @GET("api/synthese/color_taxon?orderby=cd_nom")
    fun getTaxrefAreas(
        @Query("code_area_type") codeAreaType: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null
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
    fun getApplications(): Call<ResponseBody>

    @Headers("Accept: application/json")
    @GET("api/gn_commons/get_id_table_location/pr_occtax.cor_counting_occtax")
    fun getIdTableLocation(): Call<Long>

    @Headers("Accept: application/json")
    @GET("api/gn_commons/additional_fields")
    fun getAdditionalFields(@Query("module_code") module: String): Call<ResponseBody>

    @Streaming
    @GET
    fun downloadPackage(
        @Url url: String
    ): Call<ResponseBody>
}
