package fr.geonature.sync.sync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.launch

/**
 * [PackageInfo] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PackageInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(getApplication())
    private val packageInfoManager: PackageInfoManager = PackageInfoManager.getInstance(getApplication())

    val packageInfos: LiveData<List<PackageInfo>> = packageInfoManager.packageInfos

    /**
     * Gets all compatible installed applications.
     */
    fun getInstalledApplications() {
        viewModelScope.launch {
            packageInfoManager.getInstalledApplications().asSequence().forEach {
                startSyncInputs(it)
            }
        }
    }

    private fun startSyncInputs(packageInfo: PackageInfo) {
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val continuation = workManager.beginUniqueWork(InputsSyncWorker.workName(packageInfo.packageName),
                                                       ExistingWorkPolicy.REPLACE,
                                                       OneTimeWorkRequest.Builder(InputsSyncWorker::class.java)
                                                               .addTag(InputsSyncWorker.tagName(packageInfo.packageName))
                                                               .setConstraints(constraints)
                                                               .setInputData(Data.Builder()
                                                                                     .putString(InputsSyncWorker.KEY_PACKAGE_NAME, packageInfo.packageName)
                                                                                     .build())
                                                               .build())

        // start the work
        continuation.enqueue()
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