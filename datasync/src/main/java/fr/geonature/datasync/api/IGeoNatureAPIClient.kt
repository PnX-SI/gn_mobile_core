package fr.geonature.datasync.api

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.datasync.api.model.AuthCredentials
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.NomenclatureType
import fr.geonature.datasync.api.model.Taxref
import fr.geonature.datasync.api.model.TaxrefArea
import fr.geonature.datasync.api.model.User
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
     * GeoNature base URLs.
     */
    data class ServerUrls(
        val geoNatureBaseUrl: String,
        val taxHubBaseUrl: String? = null,
    ) : Parcelable {

        private constructor(parcel: Parcel) : this(parcel.readString()!!,
            parcel.readString())

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(
            dest: Parcel?,
            flags: Int,
        ) {
            dest?.apply {
                writeString(geoNatureBaseUrl)
                writeString(taxHubBaseUrl)
            }
        }

        companion object CREATOR : Parcelable.Creator<ServerUrls> {
            override fun createFromParcel(parcel: Parcel): ServerUrls {
                return ServerUrls(parcel)
            }

            override fun newArray(size: Int): Array<ServerUrls?> {
                return arrayOfNulls(size)
            }
        }
    }

    /**
     * Returns the current base URLs.
     */
    fun getBaseUrls(): ServerUrls

    /**
     * Sets base URLs for GeoNature and TaxHub.
     *
     * @param url base URLs
     */
    fun setBaseUrls(url: ServerUrls)

    fun authLogin(authCredentials: AuthCredentials): Call<AuthLogin>

    /**
     * Performs logout.
     */
    fun logout()

    fun sendInput(
        module: String,
        input: JSONObject,
    ): Call<ResponseBody>

    fun getMetaDatasets(): Call<ResponseBody>

    fun getUsers(menuId: Int): Call<List<User>>

    fun getTaxonomyRanks(): Call<ResponseBody>

    fun getTaxref(
        listId: Int,
        limit: Int? = null,
        offset: Int? = null,
    ): Call<List<Taxref>>

    fun getTaxrefAreas(
        codeAreaType: String? = null,
        limit: Int? = null,
        offset: Int? = null,
    ): Call<List<TaxrefArea>>

    fun getNomenclatures(): Call<List<NomenclatureType>>

    fun getDefaultNomenclaturesValues(module: String): Call<ResponseBody>

    /**
     * Gets all available applications from GeoNature.
     */
    fun getApplications(): Call<ResponseBody>

    /**
     * Downloads application package (APK) from GeoNature.
     */
    fun downloadPackage(url: String): Call<ResponseBody>

    /**
     * Whether the base URLs are set.
     */
    fun checkSettings(): Boolean
}