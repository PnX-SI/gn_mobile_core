package fr.geonature.sync.sync

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import fr.geonature.commons.util.FileUtils.getInputsFolder
import kotlinx.coroutines.Dispatchers
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

    val packageInfos: MutableLiveData<List<PackageInfo>> = MutableLiveData()

    /**
     * Finds all compatible installed applications.
     */
    suspend fun getInstalledApplications(): List<PackageInfo> = withContext(Dispatchers.IO) {
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
                                getInputsFolder(applicationContext,
                                                it.packageName).walkTopDown().filter { f -> f.extension == "json" }.count(),
                                pm.getLaunchIntentForPackage(it.packageName))
                }
                .toList()
                .also {
                    packageInfos.postValue(it)
                }
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