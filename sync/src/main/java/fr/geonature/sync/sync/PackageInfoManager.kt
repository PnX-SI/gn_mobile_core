package fr.geonature.sync.sync

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.geonature.commons.util.DeviceUtils.isPostPie
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
import fr.geonature.sync.api.model.AppPackage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.json.JSONObject

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
    private val sharedUserId = pm.getPackageInfo(
        applicationContext.packageName,
        PackageManager.GET_META_DATA
    )
        .sharedUserId

    private val availablePackageInfos = mutableMapOf<String, PackageInfo>()

    private val _appPackagesToUpdate: MutableLiveData<List<AppPackage>> =
        MutableLiveData(emptyList())
    val appPackagesToUpdate: LiveData<List<AppPackage>> = _appPackagesToUpdate

    /**
     * Returns the name of this application's package.
     */
    val packageName: String = applicationContext.packageName

    /**
     * Finds all compatible installed applications.
     */
    suspend fun getInstalledApplications(): List<PackageInfo> = withContext(IO) {
        availablePackageInfos.clear()

        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { it.packageName.startsWith(sharedUserId) }
            .map {
                val packageInfoFromPackageManager = pm.getPackageInfo(
                    it.packageName,
                    PackageManager.GET_META_DATA
                )

                @Suppress("DEPRECATION")
                PackageInfo(
                    it.packageName,
                    pm.getApplicationLabel(it)
                        .toString(),
                    if (isPostPie) packageInfoFromPackageManager.longVersionCode else packageInfoFromPackageManager.versionCode.toLong(),
                    packageInfoFromPackageManager.versionName,
                    pm.getApplicationIcon(it.packageName),
                    pm.getLaunchIntentForPackage(it.packageName)
                )
            }
            .onEach { availablePackageInfos[it.packageName] = it }
            .toList()
    }

    /**
     * Gets related info from package name.
     */
    suspend fun getPackageInfo(packageName: String): PackageInfo? = withContext(IO) {
        val packageInfo = availablePackageInfos[packageName]

        if (packageInfo == null) {
            getInstalledApplications()
        }

        availablePackageInfos[packageName]
    }

    suspend fun getInputsToSynchronize(packageInfo: PackageInfo): List<SyncInput> =
        withContext(IO) {
            FileUtils.getInputsFolder(
                applicationContext,
                packageInfo.packageName
            )
                .walkTopDown()
                .filter { f -> f.isFile && f.extension == "json" && f.canRead() }
                .map {
                    val rawString = it.readText()
                    val toJson = JSONObject(rawString)
                    SyncInput(
                        packageInfo,
                        it.absolutePath,
                        toJson.getString("module"),
                        toJson
                    )
                }
                .toList()
        }

    fun setAppPackagesToUpdate(appPackages: List<AppPackage>) {
        _appPackagesToUpdate.postValue(appPackages)
    }

    fun getAppPackageToUpdate(packageName: String): AppPackage? {
        return _appPackagesToUpdate.value?.find { it.packageName == packageName }
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
