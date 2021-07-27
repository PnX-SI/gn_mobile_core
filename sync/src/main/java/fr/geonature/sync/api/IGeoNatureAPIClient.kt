package fr.geonature.sync.api

import fr.geonature.sync.api.model.AppPackage
import fr.geonature.sync.api.model.AuthCredentials
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.NomenclatureType
import fr.geonature.sync.api.model.Taxref
import fr.geonature.sync.api.model.TaxrefArea
import fr.geonature.sync.api.model.User
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

/**
 * GeoNature API client.
 *
 * @author S. Grimault
 */
interface IGeoNatureAPIClient {

    /**
     * Base URL for GeoNature.
     */
    var geoNatureBaseUrl: String?

    /**
     * Base URL for TaxHub.
     */
    var taxHubBaseUrl: String?

    fun authLogin(authCredentials: AuthCredentials): Call<AuthLogin>?

    /**
     * Performs logout.
     */
    fun logout()

    fun sendInput(
        module: String,
        input: JSONObject
    ): Call<ResponseBody>?

    fun getMetaDatasets(): Call<ResponseBody>?

    fun getUsers(menuId: Int): Call<List<User>>?

    fun getTaxonomyRanks(): Call<ResponseBody>?

    fun getTaxref(
        listId: Int,
        limit: Int? = null,
        offset: Int? = null
    ): Call<List<Taxref>>?

    fun getTaxrefAreas(
        codeAreaType: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ): Call<List<TaxrefArea>>?

    fun getNomenclatures(): Call<List<NomenclatureType>>?

    fun getDefaultNomenclaturesValues(module: String): Call<ResponseBody>?

    /**
     * Gets all available applications from GeoNature.
     */
    fun getApplications(): Call<List<AppPackage>>?

    /**
     * Downloads application package (APK) from GeoNature.
     */
    fun downloadPackage(url: String): Call<ResponseBody>?

    fun checkSettings(): Boolean
}