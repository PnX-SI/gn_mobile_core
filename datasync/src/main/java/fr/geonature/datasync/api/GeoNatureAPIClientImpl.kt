package fr.geonature.datasync.api

import fr.geonature.datasync.api.error.MissingConfigurationException
import fr.geonature.datasync.api.model.AuthCredentials
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.NomenclatureType
import fr.geonature.datasync.api.model.Taxref
import fr.geonature.datasync.api.model.TaxrefArea
import fr.geonature.datasync.api.model.User
import fr.geonature.datasync.auth.ICookieManager
import okhttp3.ResponseBody
import org.tinylog.Logger
import retrofit2.Call

/**
 * GeoNature API client.
 *
 * @author S. Grimault
 */
class GeoNatureAPIClientImpl(private val cookieManager: ICookieManager) : IGeoNatureAPIClient {

    private var geoNatureService: IGeoNatureService? = null
    private var taxHubService: ITaxHubService? = null
    private var geoNatureBaseUrl: String? = null
    private var taxHubBaseUrl: String? = null

    override fun getBaseUrls(): IGeoNatureAPIClient.ServerUrls {
        val geoNatureBaseUrl = geoNatureBaseUrl
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException
        val taxHubBaseUrl = taxHubBaseUrl
            ?: throw MissingConfigurationException.MissingTaxHubBaseURLException

        return IGeoNatureAPIClient.ServerUrls(
            geoNatureBaseUrl,
            taxHubBaseUrl
        )
    }

    override fun setBaseUrls(url: IGeoNatureAPIClient.ServerUrls) {
        Logger.info { "set server base URLs (GeoNature: '${url.geoNatureBaseUrl}'${if (url.taxHubBaseUrl.isNullOrBlank()) "" else ", TaxHub: '${url.taxHubBaseUrl}'"})..." }

        if (url.geoNatureBaseUrl.isNotBlank()) {
            this.geoNatureBaseUrl = url.geoNatureBaseUrl
            geoNatureService = createServiceClient(
                url.geoNatureBaseUrl,
                cookieManager,
                IGeoNatureService::class.java
            )
        }

        if (url.taxHubBaseUrl?.isNotBlank() == true) {
            this.taxHubBaseUrl = url.taxHubBaseUrl
            taxHubService = createServiceClient(
                url.taxHubBaseUrl,
                cookieManager,
                ITaxHubService::class.java
            )
        }
    }

    override fun authLogin(authCredentials: AuthCredentials): Call<AuthLogin> {
        val geoNatureService = geoNatureService
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException

        return geoNatureService.authLogin(authCredentials)
    }

    override fun logout() {
        cookieManager.clearCookie()
    }

    override fun getMetaDatasets(): Call<ResponseBody> {
        val geoNatureService = geoNatureService
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException

        return geoNatureService.getMetaDatasets()
    }

    override fun getUsers(menuId: Int): Call<List<User>> {
        val geoNatureService = geoNatureService
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException

        return geoNatureService.getUsers(menuId)
    }

    override fun getTaxonomyRanks(): Call<ResponseBody> {
        val taxHubService = taxHubService
            ?: throw MissingConfigurationException.MissingTaxHubBaseURLException

        return taxHubService.getTaxonomyRanks()
    }

    override fun getTaxref(
        listId: Int,
        limit: Int?,
        offset: Int?
    ): Call<List<Taxref>> {
        val taxHubService = taxHubService
            ?: throw MissingConfigurationException.MissingTaxHubBaseURLException

        return taxHubService.getTaxref(
            listId,
            limit,
            offset
        )
    }

    override fun getTaxrefAreas(
        codeAreaType: String?,
        limit: Int?,
        offset: Int?
    ): Call<List<TaxrefArea>> {
        val geoNatureService = geoNatureService
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException

        return geoNatureService.getTaxrefAreas(
            codeAreaType,
            limit,
            offset
        )
    }

    override fun getNomenclatures(): Call<List<NomenclatureType>> {
        val geoNatureService = geoNatureService
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException

        return geoNatureService.getNomenclatures()
    }

    override fun getDefaultNomenclaturesValues(module: String): Call<ResponseBody> {
        val geoNatureService = geoNatureService
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException

        return geoNatureService.getDefaultNomenclaturesValues(module)
    }

    override fun getApplications(): Call<ResponseBody> {
        val geoNatureService = geoNatureService
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException

        return geoNatureService.getApplications()
    }

    override fun downloadPackage(url: String): Call<ResponseBody> {
        val geoNatureService = geoNatureService
            ?: throw MissingConfigurationException.MissingGeoNatureBaseURLException

        return geoNatureService.downloadPackage(url)
    }

    override fun checkSettings(): Boolean {
        return geoNatureService != null && taxHubService != null
    }
}
