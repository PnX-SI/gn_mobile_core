package fr.geonature.datasync.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Failure
import fr.geonature.commons.fp.getOrElse
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.error.DataSyncSettingsNotFoundFailure
import javax.inject.Inject

/**
 * [DataSyncSettings] view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class DataSyncSettingsViewModel @Inject constructor(
    private val dataSyncSettingsRepository: IDataSyncSettingsRepository,
    private val geoNatureAPIClient: IGeoNatureAPIClient
) : ViewModel() {

    fun getDataSyncSettings(): LiveData<Either<Failure, DataSyncSettings>> = liveData {
        val dataSyncSettingsResponse = dataSyncSettingsRepository.getDataSyncSettings()

        if (dataSyncSettingsResponse.isLeft) {
            emit(dataSyncSettingsResponse)
            return@liveData
        }

        val dataSyncSettings = dataSyncSettingsResponse.getOrElse(null)

        if (dataSyncSettings == null) {
            emit(Either.Left(DataSyncSettingsNotFoundFailure()))

            return@liveData
        }

        geoNatureAPIClient.setBaseUrls(
            geoNatureBaseUrl = dataSyncSettings.geoNatureServerUrl,
            taxHubBaseUrl = dataSyncSettings.taxHubServerUrl
        )

        emit(Either.Right(dataSyncSettings))
    }

    fun getServerBaseUrls(): Either<Failure, IGeoNatureAPIClient.ServerUrls> {
        return dataSyncSettingsRepository.getServerBaseUrls()
    }

    fun setServerBaseUrls(
        geoNatureServerUrl: String,
        taxHubServerUrl: String
    ) {
        geoNatureAPIClient.setBaseUrls(
            geoNatureBaseUrl = geoNatureServerUrl,
            taxHubBaseUrl = taxHubServerUrl
        )
        dataSyncSettingsRepository.setServerBaseUrls(
            geoNatureServerUrl = geoNatureServerUrl,
            taxHubServerUrl = taxHubServerUrl
        )
    }
}