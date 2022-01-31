package fr.geonature.datasync.packageinfo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.datasync.packageinfo.worker.DownloadPackageInfoWorker
import fr.geonature.datasync.packageinfo.worker.InputsSyncWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
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

    private val _synchronizeInputsFromPackageInfo =
        map(workManager.getWorkInfosByTagLiveData(InputsSyncWorker.INPUT_SYNC_WORKER_TAG)) { workInfos ->
            val workInfoData = workInfos
                .firstOrNull()
                ?.let { workInfo ->
                    workInfo.progress
                        .getString(InputsSyncWorker.KEY_PACKAGE_NAME)
                        ?.let { workInfo.progress }
                        ?: workInfo.outputData
                            .getString(InputsSyncWorker.KEY_PACKAGE_NAME)
                            ?.let { workInfo.outputData }
                }
                ?: return@map null

            val packageName = workInfoData.getString(InputsSyncWorker.KEY_PACKAGE_NAME)
                ?: return@map null

            AppPackageInputsStatus(
                packageName,
                WorkInfo.State.values()[workInfoData.getInt(
                    InputsSyncWorker.KEY_PACKAGE_STATUS,
                    WorkInfo.State.ENQUEUED.ordinal
                )],
                workInfoData.getInt(
                    InputsSyncWorker.KEY_PACKAGE_INPUTS,
                    0
                )
            )
        }
    private val _downloadPackageInfo = MutableLiveData<AppPackageDownloadStatus>()

    /**
     * Observe all [PackageInfo] managed by this application.
     */
    val packageInfos: LiveData<List<PackageInfo>> = MediatorLiveData<List<PackageInfo>>().apply {
        postValue(emptyList())
        addSource(_allPackageInfos) { packageInfos ->
            value = packageInfos.filter { it.packageName != application.packageName }
        }
        addSource(_synchronizeInputsFromPackageInfo) { inputsStatus ->
            value = value?.map { packageInfo ->
                inputsStatus?.let {
                    if (it.packageName == inputsStatus.packageName) {
                        packageInfo
                            .copy()
                            .apply {
                                this.inputsStatus = inputsStatus
                            }
                    } else packageInfo
                }
                    ?: packageInfo
            }
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
            packageInfoRepository
                .getAllApplications()
                .collect {
                    _allPackageInfos.postValue(it)
                }
        }
    }

    /**
     * Synchronize all compatible installed applications.
     */
    fun synchronizeInstalledApplications() {
        viewModelScope.launch {
            packageInfoRepository
                .getInstalledApplications()
                .firstOrNull()
                ?.also {
                    if (it.isEmpty()) {
                        return@also
                    }

                    _allPackageInfos.postValue(it + (_allPackageInfos.value?.filter { loadedPackageInfo -> it.none { installedPackageInfo -> installedPackageInfo.packageName == loadedPackageInfo.packageName } }
                        ?: emptyList()))

                    it
                        .asSequence()
                        .filter { packageInfo -> packageInfo.packageName != getApplication<Application>().packageName }
                        .forEach { packageInfo ->
                            packageInfoRepository.updateAppSettings(packageInfo)
                            startSyncInputs(packageInfo)
                        }
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

        return map(workManager.getWorkInfoByIdLiveData(inputsSyncWorkerRequest.id)) {
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
        }.also {
            // start the work
            continuation.enqueue()
        }
    }

    fun cancelTasks() {
        workManager.cancelAllWorkByTag(InputsSyncWorker.INPUT_SYNC_WORKER_TAG)
    }

    private fun startSyncInputs(packageInfo: PackageInfo) {
        val constraints = Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputsSyncWorkerRequest = OneTimeWorkRequest
            .Builder(InputsSyncWorker::class.java)
            .addTag(InputsSyncWorker.INPUT_SYNC_WORKER_TAG)
            .setConstraints(constraints)
            .setInputData(
                Data
                    .Builder()
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
}
