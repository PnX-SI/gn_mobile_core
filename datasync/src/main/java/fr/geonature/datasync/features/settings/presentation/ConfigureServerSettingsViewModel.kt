package fr.geonature.datasync.features.settings.presentation

import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.util.PatternsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.lifecycle.BaseViewModel
import fr.geonature.datasync.R
import fr.geonature.datasync.features.settings.usecase.GetAppSettingsFromRemoteUseCase
import fr.geonature.datasync.settings.DataSyncSettings
import javax.inject.Inject

/**
 * Configure server settings view model.
 *
 * @author S. Grimault
 *
 * @see ConfigureServerSettingsActivity
 * @see GetAppSettingsFromRemoteUseCase
 */
@HiltViewModel
class ConfigureServerSettingsViewModel @Inject constructor(private val getAppSettingsFromRemoteUseCase: GetAppSettingsFromRemoteUseCase) :
    BaseViewModel() {

    private val _formState = MutableLiveData<FormState>()
    val formState: LiveData<FormState> = _formState

    private val _dataSyncSettingsLoaded = MutableLiveData<DataSyncSettings>()
    val dataSyncSettingLoaded: LiveData<DataSyncSettings> = _dataSyncSettingsLoaded

    fun validateForm(
        serverBaseUrl: String?,
        submitted: Boolean = false
    ) {
        val serverUrl =
            serverBaseUrl?.let { "${Uri.parse(serverBaseUrl).scheme?.run { "" } ?: "https://"}$serverBaseUrl" }

        if (serverUrl.isNullOrBlank() || !PatternsCompat.WEB_URL
                .matcher(serverUrl)
                .matches()
        ) {
            _formState.postValue(FormState.FormStateError(error = R.string.settings_server_form_url_invalid))

            return
        }

        _formState.postValue(
            if (submitted) FormState.FormStateSubmitted(serverBaseUrl = serverUrl)
            else FormState.FormStateValid(serverBaseUrl = serverUrl)
        )
    }

    fun loadAppSettings(serverBaseUrl: String) {
        getAppSettingsFromRemoteUseCase(
            serverBaseUrl,
            viewModelScope,
        ) {
            it.fold(
                ::handleFailure,
                ::handleDataSyncSettings,
            )
        }
    }

    private fun handleDataSyncSettings(dataSyncSettings: DataSyncSettings) {
        _dataSyncSettingsLoaded.value = dataSyncSettings
    }

    /**
     * Data validation state of the server settings form.
     *
     * @author S. Grimault
     */
    sealed class FormState {
        data class FormStateError(@StringRes val error: Int) : FormState()
        data class FormStateValid(val serverBaseUrl: String) : FormState()
        data class FormStateSubmitted(val serverBaseUrl: String) : FormState()
    }
}