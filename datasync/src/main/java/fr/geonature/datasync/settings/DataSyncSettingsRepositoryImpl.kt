package fr.geonature.datasync.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.Failure
import fr.geonature.commons.fp.getOrElse
import fr.geonature.datasync.R
import fr.geonature.datasync.api.GeoNatureMissingConfigurationFailure
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.error.DataSyncSettingsJsonParseFailure
import fr.geonature.datasync.error.DataSyncSettingsNotFoundFailure
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException

/**
 * Default implementation of [IDataSyncSettingsRepository].
 *
 * @author S. Grimault
 */
class DataSyncSettingsRepositoryImpl(
    private val applicationContext: Context,
    private val dataSyncSettingsDataSource: IDataSyncSettingsDataSource
) : IDataSyncSettingsRepository {

    private val preferenceManager: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(applicationContext)

    private var _dataSyncSettings: Either<Failure, DataSyncSettings>? = null
        set(value) {
            field = value
            value?.also {
                _dataSyncSettingsLiveData.postValue(it)
            }
        }

    private val _dataSyncSettingsLiveData: MutableLiveData<Either<Failure, DataSyncSettings>> =
        MutableLiveData()
    override val dataSyncSettings: LiveData<Either<Failure, DataSyncSettings>> =
        _dataSyncSettingsLiveData

    override suspend fun getDataSyncSettings(): Either<Failure, DataSyncSettings> {
        // load settings from data source
        val dataSyncSettingsLoadedResponse =
            runCatching { dataSyncSettingsDataSource.load() }.fold(onSuccess = { value: DataSyncSettings ->
                Right(value)
            },
                onFailure = { exception: Throwable ->
                    Left(
                        if (exception is DataSyncSettingsNotFoundException) DataSyncSettingsNotFoundFailure(exception.source)
                        else DataSyncSettingsJsonParseFailure
                    )
                })

        if (dataSyncSettingsLoadedResponse.isLeft) {
            return dataSyncSettingsLoadedResponse.also {
                _dataSyncSettings = it
            }
        }

        val dataSyncSettingsLoaded = dataSyncSettingsLoadedResponse.getOrElse(null)
            ?: return Left(DataSyncSettingsNotFoundFailure()).also {
                _dataSyncSettings = it
            }

        val currentGeoNatureBaseUrl = preferenceManager.getString(
            applicationContext.getString(R.string.preference_category_server_geonature_url_key),
            null
        )
        val currentTaxHubBaseUrl = preferenceManager.getString(
            applicationContext.getString(R.string.preference_category_server_taxhub_url_key),
            null
        )

        _dataSyncSettings =
            Right(if (currentGeoNatureBaseUrl.isNullOrBlank() || currentTaxHubBaseUrl.isNullOrBlank()) {
                // update preferences from loaded settings
                setServerBaseUrls(
                    geoNatureServerUrl = dataSyncSettingsLoaded.geoNatureServerUrl,
                    taxHubServerUrl = dataSyncSettingsLoaded.taxHubServerUrl
                )

                dataSyncSettingsLoaded
            } else {
                runCatching {
                    // update loaded settings from preferences
                    DataSyncSettings
                        .Builder()
                        .from(dataSyncSettingsLoaded)
                        .serverUrls(
                            geoNatureServerUrl = currentGeoNatureBaseUrl,
                            taxHubServerUrl = currentTaxHubBaseUrl
                        )
                        .build()
                }
                    .onFailure {
                        setServerBaseUrls(
                            "",
                            ""
                        )
                    }
                    .getOrDefault(dataSyncSettingsLoaded)
            })

        return _dataSyncSettings
            ?: Left(DataSyncSettingsNotFoundFailure())
    }

    override fun getServerBaseUrls(): Either<Failure, IGeoNatureAPIClient.ServerUrls> {
        val currentDataSyncSettings = _dataSyncSettings?.getOrElse(null)

        val geoNatureBaseUrl = preferenceManager.getString(
            applicationContext.getString(R.string.preference_category_server_geonature_url_key),
            currentDataSyncSettings?.geoNatureServerUrl
        )
        val taxHubServerUrl = preferenceManager.getString(
            applicationContext.getString(R.string.preference_category_server_taxhub_url_key),
            currentDataSyncSettings?.taxHubServerUrl
        )

        if (geoNatureBaseUrl.isNullOrBlank() || taxHubServerUrl.isNullOrBlank()) {
            return Left(GeoNatureMissingConfigurationFailure)
        }

        return Right(
            IGeoNatureAPIClient.ServerUrls(
                geoNatureBaseUrl = geoNatureBaseUrl,
                taxHubBaseUrl = taxHubServerUrl
            )
        )
    }

    override fun setServerBaseUrls(
        geoNatureServerUrl: String,
        taxHubServerUrl: String
    ) {
        preferenceManager.edit {
            if (geoNatureServerUrl.isBlank()) remove(applicationContext.getString(R.string.preference_category_server_geonature_url_key))
            else putString(
                applicationContext.getString(R.string.preference_category_server_geonature_url_key),
                geoNatureServerUrl
            )

            if (taxHubServerUrl.isBlank()) remove(applicationContext.getString(R.string.preference_category_server_taxhub_url_key))
            else putString(
                applicationContext.getString(R.string.preference_category_server_taxhub_url_key),
                taxHubServerUrl
            )
        }

        if (geoNatureServerUrl.isBlank() || taxHubServerUrl.isBlank()) {
            return
        }

        val currentDataSyncSettings = _dataSyncSettings?.getOrElse(null)
            ?: return

        // do nothing if the current loaded settings remain the same
        if (currentDataSyncSettings.geoNatureServerUrl == geoNatureServerUrl && currentDataSyncSettings.taxHubServerUrl == taxHubServerUrl) {
            return
        }

        _dataSyncSettings = Right(
            DataSyncSettings
                .Builder()
                .from(currentDataSyncSettings)
                .serverUrls(
                    geoNatureServerUrl = geoNatureServerUrl,
                    taxHubServerUrl = taxHubServerUrl
                )
                .build()
        )
    }
}