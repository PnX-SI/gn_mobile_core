package fr.geonature.datasync.features.settings.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.commons.lifecycle.BaseViewModel
import fr.geonature.datasync.features.settings.usecase.UpdateSettingsFromRemoteUseCase
import fr.geonature.datasync.packageinfo.PackageInfo
import javax.inject.Inject

/**
 * Check and update app settings view model.
 *
 * @author S. Grimault
 *
 * @see UpdateSettingsFromRemoteUseCase
 */
@HiltViewModel
class UpdateSettingsViewModel @Inject constructor(private val updateSettingsFromRemoteUseCase: UpdateSettingsFromRemoteUseCase) :
    BaseViewModel() {

    private val _packageInfoUpdated: MutableLiveData<PackageInfo> = MutableLiveData()
    val packageInfoUpdated: LiveData<PackageInfo> = _packageInfoUpdated

    fun updateAppSettings() {
        updateSettingsFromRemoteUseCase(
            BaseUseCase.None(),
            viewModelScope,
        ) {
            it.fold(
                ::handleFailure,
                ::handlePackageInfoUpdated
            )
        }
    }

    private fun handlePackageInfoUpdated(packageInfo: PackageInfo) {
        _packageInfoUpdated.postValue(packageInfo)
    }
}