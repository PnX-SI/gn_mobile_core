package fr.geonature.datasync.api

import android.webkit.MimeTypeMap
import fr.geonature.datasync.api.error.MissingConfigurationException
import fr.geonature.datasync.api.model.AuthCredentials
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.Media
import fr.geonature.datasync.api.model.NomenclatureType
import fr.geonature.datasync.api.model.Taxref
import fr.geonature.datasync.api.model.TaxrefArea
import fr.geonature.datasync.api.model.TaxrefVersion
import fr.geonature.datasync.api.model.User
import fr.geonature.datasync.auth.ICookieManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.tinylog.Logger
import retrofit2.Call
import java.io.File

/**
 * Default implementation of _GeoNature_ API client.
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

    override fun sendMediaFile(
        mediaType: Long,
        tableLocation: Long,
        author: String,
        titleEn: String?,
        titleFr: String?,
        descriptionEn: String?,
        descriptionFr: String?,
        mediaFile: File
    ): Call<Media> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.sendMediaFile(
            mediaType,
            tableLocation,
            author.toRequestBody(),
            titleEn?.toRequestBody(),
            titleFr?.toRequestBody(),
            descriptionEn?.toRequestBody(),
            descriptionFr?.toRequestBody(),
            MultipartBody.Part.createFormData(
                "file",
                mediaFile.name,
                mediaFile.asRequestBody((mediaFile
                    .toURI()
                    .toURL()
                    .openConnection().contentType
                    ?: mediaFile.extension
                        .takeIf { it.isNotEmpty() }
                        ?.let {
                            MimeTypeMap
                                .getSingleton()
                                .getMimeTypeFromExtension(it)
                        })?.toMediaTypeOrNull()
                ),
            ),
        )
    }

    override fun deleteMediaFile(mediaId: Int): Call<ResponseBody> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.deleteMediaFile(mediaId)
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

    override fun getTaxrefVersion(): Call<TaxrefVersion> {
        val taxHubService = taxHubService
            ?: throw MissingConfigurationException.MissingTaxHubBaseURLException

        return taxHubService.getTaxrefVersion()
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

    override fun getIdTableLocation(): Call<Long> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.getIdTableLocation()
    }

    override fun getAdditionalFields(module: String): Call<ResponseBody> {
        val geoNatureService = geoNatureService
            ?: throw IllegalStateException("missing GeoNature base URL")

        return geoNatureService.getAdditionalFields(module)
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
