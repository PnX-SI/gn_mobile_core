package fr.geonature.sync.sync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import fr.geonature.sync.api.model.AppPackage
import fr.geonature.sync.sync.worker.CheckAppPackagesWorker
import fr.geonature.sync.sync.worker.DownloadPackageWorker
import fr.geonature.sync.sync.worker.InputsSyncWorker
import kotlinx.coroutines.launch

/**
 * [PackageInfo] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PackageInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(getApplication())
    private val packageInfoManager: PackageInfoManager =
        PackageInfoManager.getInstance(getApplication())

    private val _packageInfos: MutableLiveData<List<PackageInfo>> = MutableLiveData()
    val packageInfos: LiveData<List<PackageInfo>> =
        switchMap(_packageInfos) { packageInfos ->
            map(workManager.getWorkInfosByTagLiveData(InputsSyncWorker.INPUT_SYNC_WORKER_TAG)) { workInfos ->
                packageInfos.asSequence()
                    .map { packageInfo ->
                        packageInfo.copy()
                            .apply {
                                val workInfo =
                                    workInfos.firstOrNull { workInfo -> workInfo.progress.getString(InputsSyncWorker.KEY_PACKAGE_NAME) == packageName }
                                        ?: workInfos.firstOrNull { workInfo -> workInfo.outputData.getString(InputsSyncWorker.KEY_PACKAGE_NAME) == packageName }

                                if (workInfo != null) {
                                    state = WorkInfo.State.values()[workInfo.progress.getInt(
                                        InputsSyncWorker.KEY_PACKAGE_STATUS,
                                        workInfo.outputData.getInt(
                                            InputsSyncWorker.KEY_PACKAGE_STATUS,
                                            WorkInfo.State.ENQUEUED.ordinal
                                        )
                                    )]
                                    inputs = workInfo.progress.getInt(
                                        InputsSyncWorker.KEY_PACKAGE_INPUTS,
                                        0
                                    )
                                }
                            }
                    }
                    .toList()
            }
        }
    val updateAvailable: LiveData<AppPackage?> =
        map(packageInfoManager.appPackagesToUpdate) { appPackagesToUpdate -> appPackagesToUpdate.find { it.packageName == packageInfoManager.packageName } }

    val appPackageDownloadStatus: LiveData<AppPackageDownloadStatus?> =
        map(workManager.getWorkInfosByTagLiveData(DownloadPackageWorker.DOWNLOAD_PACKAGE_WORKER_TAG)) { workInfos ->
            val workInfo = workInfos.firstOrNull() ?: return@map null

            val packageName = workInfo.progress.getString(DownloadPackageWorker.KEY_PACKAGE_NAME)
                ?: workInfo.outputData.getString(DownloadPackageWorker.KEY_PACKAGE_NAME)
                ?: return@map null

            AppPackageDownloadStatus(
                workInfo.state,
                packageName,
                workInfo.outputData.getInt(
                    DownloadPackageWorker.KEY_PROGRESS,
                    -1
                )
                    .takeIf { it > 0 }
                    ?: workInfo.progress.getInt(
                        DownloadPackageWorker.KEY_PROGRESS,
                        -1
                    ),
                workInfo.outputData.getString(DownloadPackageWorker.KEY_APK_FILE_PATH)
            )
        }

    /**
     * Checks if we can perform an update of existing apps.
     */
    fun checkAppPackages() {
        val dataSyncWorkRequest = OneTimeWorkRequest.Builder(CheckAppPackagesWorker::class.java)
            .addTag(CheckAppPackagesWorker.CHECK_APP_PACKAGES_WORKER_TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        val continuation = workManager.beginUniqueWork(
            CheckAppPackagesWorker.CHECK_APP_PACKAGES_WORKER,
            ExistingWorkPolicy.KEEP,
            dataSyncWorkRequest
        )

        // start the work
        continuation.enqueue()
    }

    /**
     * Gets all compatible installed applications.
     */
    fun getInstalledApplicationsToSynchronize() {
        viewModelScope.launch {
            val packageInfos = packageInfoManager.getInstalledApplications()
                .filter { it.packageName != packageInfoManager.packageName }
            _packageInfos.postValue(packageInfos)
            packageInfos.forEach { startSyncInputs(it) }
        }
    }

    fun downloadAppPackage(appPackage: AppPackage) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputsSyncWorkerRequest = OneTimeWorkRequest.Builder(DownloadPackageWorker::class.java)
            .addTag(DownloadPackageWorker.DOWNLOAD_PACKAGE_WORKER_TAG)
            .setConstraints(constraints)
            .setInputData(
                Data.Builder()
                    .putString(
                        DownloadPackageWorker.KEY_PACKAGE_NAME,
                        appPackage.packageName
                    )
                    .build()
            )
            .build()

        val continuation = workManager.beginUniqueWork(
            DownloadPackageWorker.workName(appPackage.packageName),
            ExistingWorkPolicy.KEEP,
            inputsSyncWorkerRequest
        )

        // start the work
        continuation.enqueue()
    }

    fun cancelTasks() {
        workManager.cancelAllWorkByTag(InputsSyncWorker.INPUT_SYNC_WORKER_TAG)
    }

    private fun startSyncInputs(packageInfo: PackageInfo) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputsSyncWorkerRequest = OneTimeWorkRequest.Builder(InputsSyncWorker::class.java)
            .addTag(InputsSyncWorker.INPUT_SYNC_WORKER_TAG)
            .setConstraints(constraints)
            .setInputData(
                Data.Builder()
                    .putString(
                        InputsSyncWorker.KEY_PACKAGE_NAME,
                        packageInfo.packageName
                    )
                    .build()
            )
            .build()

        val continuation = workManager.beginUniqueWork(
            InputsSyncWorker.workName(packageInfo.packageName),
            ExistingWorkPolicy.KEEP,
            inputsSyncWorkerRequest
        )

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
