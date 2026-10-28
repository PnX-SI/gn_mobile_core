package fr.geonature.auth.settings.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.auth.settings.domain.AuthSettings
import fr.geonature.auth.settings.repository.IAuthSettingsRepository
import javax.inject.Inject

/**
 * [AuthSettings] view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class AuthSettingsViewModel @Inject constructor(private val authSettingsRepository: IAuthSettingsRepository) :
    ViewModel() {

    /**
     * Loads [AuthSettings] from local `JSON` file.
     */
    fun getAuthSettings(): LiveData<Result<AuthSettings>> = liveData {
        emit(authSettingsRepository.getAuthSettings())
    }
}
