package fr.geonature.sync.sync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
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
import fr.geonature.sync.BuildConfig
import fr.geonature.sync.sync.worker.DownloadPackageWorker
import fr.geonature.sync.sync.worker.InputsSyncWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * [PackageInfo] view model.
 *
 * @author S. Grimault
 */
class PackageInfoViewModel(
    application: Application,
    private val packageInfoManager: IPackageInfoManager
) : AndroidViewModel(application) {

    private val workManager: WorkManager = WorkManager.getInstance(getApplication())
    private val _appSettingsUpdated: MutableLiveData<Boolean> = MutableLiveData()
    private val _allPackageInfos: MutableLiveData<List<PackageInfo>> = MutableLiveData()

    private val _synchronizeInputsFromPackageInfo = map(workManager.getWorkInfosByTagLiveData(InputsSyncWorker.INPUT_SYNC_WORKER_TAG)) { workInfos ->
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
            value = packageInfos.filter { it.packageName != BuildConfig.APPLICATION_ID }
        }
        addSource(_synchronizeInputsFromPackageInfo) { inputsStatus ->
            value = value?.map { packageInfo ->
                inputsStatus?.let {
                    if (it.packageName == inputsStatus.packageName) {
                        packageInfo.copy().apply {
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
                        packageInfo.copy().apply {
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
                    .find { it.packageName == BuildConfig.APPLICATION_ID }
                    ?.also {
                        if (it.settings != null) {
                            packageInfoManager.updateAppSettings(it)
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
            packageInfoManager
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
            _allPackageInfos.value
                ?.asSequence()
                ?.filter { it.packageName != BuildConfig.APPLICATION_ID }
                ?.forEach {
                    packageInfoManager.updateAppSettings(it)
                    startSyncInputs(it)
                }
        }
    }

    fun downloadAppPackage(packageName: String): LiveData<AppPackageDownloadStatus?> {
        val constraints = Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputsSyncWorkerRequest = OneTimeWorkRequest
            .Builder(DownloadPackageWorker::class.java)
            .addTag(DownloadPackageWorker.DOWNLOAD_PACKAGE_WORKER_TAG)
            .setConstraints(constraints)
            .setInputData(
                Data
                    .Builder()
                    .putString(
                        DownloadPackageWorker.KEY_PACKAGE_NAME,
                        packageName
                    )
                    .build()
            )
            .build()

        val continuation = workManager.beginUniqueWork(
            DownloadPackageWorker.workName(packageName),
            ExistingWorkPolicy.REPLACE,
            inputsSyncWorkerRequest
        )

        return map(workManager.getWorkInfoByIdLiveData(inputsSyncWorkerRequest.id)) {
            if (it == null) {
                return@map null
            }

            val packageNameToUpgrade = it.progress.getString(DownloadPackageWorker.KEY_PACKAGE_NAME)
                ?: it.outputData.getString(DownloadPackageWorker.KEY_PACKAGE_NAME)
                ?: return@map null

            val downloadStatus = AppPackageDownloadStatus(packageNameToUpgrade,
                it.state,
                it.outputData
                    .getInt(
                        DownloadPackageWorker.KEY_PROGRESS,
                        -1
                    )
                    .takeIf { progress -> progress > 0 }
                    ?: it.progress.getInt(
                        DownloadPackageWorker.KEY_PROGRESS,
                        -1
                    ),
                it.outputData.getString(DownloadPackageWorker.KEY_APK_FILE_PATH))

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

    /**
     * Default Factory to use for [PackageInfoViewModel].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    class Factory(val creator: () -> PackageInfoViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }
}
