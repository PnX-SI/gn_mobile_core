package fr.geonature.datasync.packageinfo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.datasync.packageinfo.worker.DownloadPackageInfoWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [PackageInfo] view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class PackageInfoViewModel @Inject constructor(
    application: Application,
    private val packageInfoRepository: IPackageInfoRepository
) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(getApplication())
    private val _appSettingsUpdated: MutableLiveData<Boolean> = MutableLiveData()
    private val _allPackageInfos: MutableLiveData<List<PackageInfo>> = MutableLiveData(emptyList())

    private val _downloadPackageInfo = MutableLiveData<AppPackageDownloadStatus>()

    /**
     * Observe all [PackageInfo] managed by this application.
     */
    val packageInfos: LiveData<List<PackageInfo>> = MediatorLiveData<List<PackageInfo>>().apply {
        postValue(emptyList())
        addSource(_allPackageInfos) { packageInfos ->
            value = packageInfos
        }
        addSource(_downloadPackageInfo) { downloadStatus ->
            value = value?.map { packageInfo ->
                downloadStatus?.let {
                    if (it.packageName == downloadStatus.packageName) {
                        packageInfo
                            .copy()
                            .apply {
                                this.downloadStatus = downloadStatus
                            }
                    } else packageInfo
                }
                    ?: packageInfo
            }
        }
    }

    /**
     * Checks if the current app can be updated or not.
     */
    val updateAvailable: LiveData<PackageInfo> = MediatorLiveData<PackageInfo>().apply {
        addSource(_allPackageInfos) { packageInfos: List<PackageInfo> ->
            viewModelScope.launch {
                packageInfos
                    .find { it.packageName == application.packageName }
                    ?.also {
                        if (it.settings != null) {
                            packageInfoRepository.updateAppSettings(it)
                            delay(250)
                            _appSettingsUpdated.postValue(true)
                        }

                        if (it.hasNewVersionAvailable()) {
                            value = it
                        }
                    }
            }
        }
    }

    val appSettingsUpdated: LiveData<Boolean> = _appSettingsUpdated

    /**
     * Finds all available applications from GeoNature and these installed locally.
     */
    fun getAllApplications() {
        viewModelScope.launch {
            val allPackageInfos = packageInfoRepository.getAllApplications()
            _allPackageInfos.postValue(allPackageInfos)
        }
    }

    /**
     * Synchronize all compatible installed applications.
     */
    fun synchronizeInstalledApplications() {
        viewModelScope.launch {
            val installedPackageInfos = packageInfoRepository.getInstalledApplications()

            if (installedPackageInfos.isEmpty()) {
                return@launch
            }

            _allPackageInfos.postValue(installedPackageInfos + (_allPackageInfos.value?.filter { loadedPackageInfo -> installedPackageInfos.none { installedPackageInfo -> installedPackageInfo.packageName == loadedPackageInfo.packageName } }
                ?: emptyList()))

            installedPackageInfos
                .asSequence()
                .forEach { packageInfo ->
                    packageInfoRepository.updateAppSettings(packageInfo)
                    startSyncInputs(packageInfo)
                }
        }
    }

    fun downloadAppPackage(packageName: String): LiveData<AppPackageDownloadStatus?> {
        val constraints = Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputsSyncWorkerRequest = OneTimeWorkRequest
            .Builder(DownloadPackageInfoWorker::class.java)
            .addTag(DownloadPackageInfoWorker.DOWNLOAD_PACKAGE_WORKER_TAG)
            .setConstraints(constraints)
            .setInputData(
                Data
                    .Builder()
                    .putString(
                        DownloadPackageInfoWorker.KEY_PACKAGE_NAME,
                        packageName
                    )
                    .build()
            )
            .build()

        val continuation = workManager.beginUniqueWork(
            DownloadPackageInfoWorker.workName(packageName),
            ExistingWorkPolicy.REPLACE,
            inputsSyncWorkerRequest
        )

        return workManager
            .getWorkInfoByIdLiveData(inputsSyncWorkerRequest.id)
            .map {
                if (it == null) {
                    return@map null
                }

                val packageNameToUpgrade =
                    it.progress.getString(DownloadPackageInfoWorker.KEY_PACKAGE_NAME)
                        ?: it.outputData.getString(DownloadPackageInfoWorker.KEY_PACKAGE_NAME)
                        ?: return@map null

                val downloadStatus = AppPackageDownloadStatus(packageNameToUpgrade,
                    it.state,
                    it.outputData
                        .getInt(
                            DownloadPackageInfoWorker.KEY_PROGRESS,
                            -1
                        )
                        .takeIf { progress -> progress > 0 }
                        ?: it.progress.getInt(
                            DownloadPackageInfoWorker.KEY_PROGRESS,
                            -1
                        ),
                    it.outputData.getString(DownloadPackageInfoWorker.KEY_APK_FILE_PATH))

                _downloadPackageInfo.postValue(downloadStatus)

                downloadStatus
            }
            .also {
                // start the work
                continuation.enqueue()
            }
    }

    fun cancelTasks() {
    }

    private fun startSyncInputs(packageInfo: PackageInfo) {
    }
}
