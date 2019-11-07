package fr.geonature.sync.sync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * [PackageInfo] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PackageInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val packageInfoManager: PackageInfoManager = PackageInfoManager.getInstance(getApplication())

    /**
     * Gets all compatible installed applications.
     */
    fun getInstalledApplications(): LiveData<List<PackageInfo>> {
        viewModelScope.launch {
            packageInfoManager.getInstalledApplications()
        }

        return packageInfoManager.packageInfos
    }

    /**
     * Default Factory to use for [PackageInfoViewModel].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    class Factory(val creator: () -> PackageInfoViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }
}