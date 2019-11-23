package fr.geonature.sync.sync

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkInfo
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

/**
 * [PackageInfo] manager.
 *
 * Retrieves various kinds of information related to the application packages that are currently
 * installed on the device.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PackageInfoManager private constructor(private val applicationContext: Context) {

    private val pm = applicationContext.packageManager
    private val sharedUserId = pm.getPackageInfo(applicationContext.packageName,
                                                 PackageManager.GET_META_DATA)
            .sharedUserId

    private val availablePackageInfos = mutableMapOf<String, PackageInfo>()
    private val _packageInfos: MutableLiveData<List<PackageInfo>> = MutableLiveData()
    val packageInfos: LiveData<List<PackageInfo>> = _packageInfos

    /**
     * Finds all compatible installed applications.
     */
    suspend fun getInstalledApplications(): List<PackageInfo> = withContext(IO) {
        availablePackageInfos.clear()

        pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .asSequence()
                .filter { it.packageName.startsWith(sharedUserId) }
                .filter { it.packageName != applicationContext.packageName }
                .map {
                    PackageInfo(it.packageName,
                                pm.getApplicationLabel(it).toString(),
                                pm.getPackageInfo(it.packageName,
                                                  PackageManager.GET_META_DATA).versionName,
                                pm.getApplicationIcon(it.packageName),
                                pm.getLaunchIntentForPackage(it.packageName))
                }
                .onEach { availablePackageInfos[it.packageName] = it }
                .toList()
                .also {
                    _packageInfos.postValue(it)
                }
    }

    /**
     * Updates given [PackageInfo] status.
     */
    fun updatePackageInfo(packageName: String,
                          state: WorkInfo.State,
                          syncInputs: List<SyncInput>): PackageInfo? {
        val packageInfoToUpdate = availablePackageInfos[packageName]?.copy()?.apply {
            syncInputs.let {
                inputs.also {
                    it.clear()
                    it.addAll(syncInputs)
                }
            }

            this.state = state
        } ?: return null

        availablePackageInfos[packageName] = packageInfoToUpdate
        _packageInfos.postValue(availablePackageInfos.values.toList())

        return packageInfoToUpdate
    }

    companion object {

        @Volatile
        private var INSTANCE: PackageInfoManager? = null

        /**
         * Gets the singleton instance of [PackageInfoManager].
         *
         * @param applicationContext The main application context.
         *
         * @return The singleton instance of [PackageInfoManager].
         */
        fun getInstance(applicationContext: Context): PackageInfoManager = INSTANCE
            ?: synchronized(this) {
                INSTANCE ?: PackageInfoManager(applicationContext).also { INSTANCE = it }
            }
    }
}