package fr.geonature.datasync.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Failure
import fr.geonature.datasync.api.IGeoNatureAPIClient
import kotlinx.coroutines.launch
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

    val dataSyncSettings: LiveData<Either<Failure, DataSyncSettings>> =
        dataSyncSettingsRepository.dataSyncSettings

    init {
        viewModelScope.launch {
            dataSyncSettingsRepository.getDataSyncSettings()
        }
    }

    fun setServerUrls(
        geoNatureServerUrl: String,
        taxHubServerUrl: String
    ) {
        geoNatureAPIClient.setBaseUrls(
            geoNatureBaseUrl = geoNatureServerUrl,
            taxHubBaseUrl = taxHubServerUrl
        )
        dataSyncSettingsRepository.setServerUrls(
            geoNatureServerUrl = geoNatureServerUrl,
            taxHubServerUrl = taxHubServerUrl
        )
    }
}