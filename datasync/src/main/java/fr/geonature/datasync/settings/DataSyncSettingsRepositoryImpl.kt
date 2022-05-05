package fr.geonature.datasync.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.orNull
import fr.geonature.datasync.R
import fr.geonature.datasync.api.GeoNatureMissingConfigurationFailure
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.settings.error.DataSyncSettingsJsonParseFailure
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundFailure

/**
 * Default implementation of [IDataSyncSettingsRepository].
 *
 * @author S. Grimault
 */
class DataSyncSettingsRepositoryImpl(
    private val applicationContext: Context,
    private val dataSyncSettingsDataSource: IDataSyncSettingsDataSource,
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
        val currentGeoNatureBaseUrl = preferenceManager.getString(
            applicationContext.getString(R.string.preference_category_server_geonature_url_key),
            null,
        )

        // load settings from data source
        val dataSyncSettingsLoadedResponse =
            runCatching { dataSyncSettingsDataSource.load() }.fold(onSuccess = { value: DataSyncSettings ->
                Right(value)
            },
                onFailure = { exception: Throwable ->
                    Left(
                        if (exception is DataSyncSettingsNotFoundException) DataSyncSettingsNotFoundFailure(
                            exception.source,
                            geoNatureBaseUrl = currentGeoNatureBaseUrl,
                        )
                        else DataSyncSettingsJsonParseFailure
                    )
                })

        if (dataSyncSettingsLoadedResponse.isLeft) {
            return dataSyncSettingsLoadedResponse.also {
                _dataSyncSettings = it
            }
        }

        val dataSyncSettingsLoaded = dataSyncSettingsLoadedResponse.orNull()
            ?: return Left(DataSyncSettingsNotFoundFailure(geoNatureBaseUrl = currentGeoNatureBaseUrl)).also {
                _dataSyncSettings = it
            }

        _dataSyncSettings = Right(if (currentGeoNatureBaseUrl.isNullOrBlank()) {
            // update preferences from loaded settings
            setServerBaseUrl(dataSyncSettingsLoaded.geoNatureServerUrl)
            dataSyncSettingsLoaded
        } else {
            runCatching {
                // update loaded settings from preferences
                DataSyncSettings
                    .Builder()
                    .from(dataSyncSettingsLoaded)
                    .serverUrls(
                        geoNatureServerUrl = currentGeoNatureBaseUrl,
                        taxHubServerUrl = dataSyncSettingsLoaded.taxHubServerUrl
                    )
                    .build()
            }
                .onFailure {
                    setServerBaseUrl("")
                }
                .getOrDefault(dataSyncSettingsLoaded)
        })

        return _dataSyncSettings
            ?: Left(DataSyncSettingsNotFoundFailure(geoNatureBaseUrl = currentGeoNatureBaseUrl))
    }

    override fun getServerBaseUrls(): Either<Failure, IGeoNatureAPIClient.ServerUrls> {
        val currentDataSyncSettings = _dataSyncSettings?.orNull()

        val geoNatureBaseUrl = preferenceManager.getString(
            applicationContext.getString(R.string.preference_category_server_geonature_url_key),
            currentDataSyncSettings?.geoNatureServerUrl
        )
        val taxHubServerUrl = currentDataSyncSettings?.taxHubServerUrl

        if (geoNatureBaseUrl.isNullOrBlank()) {
            return Left(GeoNatureMissingConfigurationFailure)
        }

        return Right(
            IGeoNatureAPIClient.ServerUrls(
                geoNatureBaseUrl = geoNatureBaseUrl,
                taxHubBaseUrl = taxHubServerUrl
            )
        )
    }

    override fun setServerBaseUrl(geoNatureServerUrl: String) {
        preferenceManager.edit {
            if (geoNatureServerUrl.isBlank()) remove(applicationContext.getString(R.string.preference_category_server_geonature_url_key))
            else putString(
                applicationContext.getString(R.string.preference_category_server_geonature_url_key),
                geoNatureServerUrl
            )
        }

        if (geoNatureServerUrl.isBlank()) {
            return
        }

        val currentDataSyncSettings = _dataSyncSettings?.orNull()
            ?: return

        // do nothing if the current loaded settings remain the same
        if (currentDataSyncSettings.geoNatureServerUrl == geoNatureServerUrl) {
            return
        }

        _dataSyncSettings = Right(
            DataSyncSettings
                .Builder()
                .from(currentDataSyncSettings)
                .serverUrls(
                    geoNatureServerUrl = geoNatureServerUrl,
                    taxHubServerUrl = currentDataSyncSettings.taxHubServerUrl
                )
                .build()
        )
    }
}