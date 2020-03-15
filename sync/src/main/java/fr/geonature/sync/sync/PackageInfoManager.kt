package fr.geonature.sync.sync

import android.content.Context
import android.content.pm.PackageManager
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
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
                PackageInfo(
                    it.packageName,
                    pm.getApplicationLabel(it)
                        .toString(),
                    pm.getPackageInfo(
                        it.packageName,
                        PackageManager.GET_META_DATA
                    ).versionName,
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
