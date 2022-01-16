package fr.geonature.datasync.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.Failure
import fr.geonature.commons.fp.getOrElse
import fr.geonature.datasync.R
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
        if (_dataSyncSettings == null || _dataSyncSettings?.isLeft != false) {
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

            _dataSyncSettings = dataSyncSettingsLoadedResponse

            if (dataSyncSettingsLoadedResponse.isLeft) {
                return dataSyncSettingsLoadedResponse
            }

            val dataSyncSettingsLoaded = dataSyncSettingsLoadedResponse.getOrElse(null)
                ?: return Left(DataSyncSettingsNotFoundFailure())

            setServerUrls(
                geoNatureServerUrl = dataSyncSettingsLoaded.geoNatureServerUrl,
                taxHubServerUrl = dataSyncSettingsLoaded.taxHubServerUrl
            )
        }

        return _dataSyncSettings
            ?: Left(DataSyncSettingsNotFoundFailure())
    }

    override fun setServerUrls(
        geoNatureServerUrl: String,
        taxHubServerUrl: String
    ) {
        preferenceManager
            .edit()
            .putString(
                applicationContext.getString(R.string.preference_category_server_geonature_url_key),
                geoNatureServerUrl
            )
            .putString(
                applicationContext.getString(R.string.preference_category_server_taxhub_url_key),
                taxHubServerUrl
            )
            .apply()

        if (geoNatureServerUrl.isBlank() || taxHubServerUrl.isBlank()) {
            return
        }

        val currentDataSyncSettings = _dataSyncSettings?.getOrElse(null)
            ?: return

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