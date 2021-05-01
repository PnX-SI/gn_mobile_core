package fr.geonature.sync.sync

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
import fr.geonature.sync.api.GeoNatureAPIClient
import fr.geonature.sync.sync.io.AppSettingsJsonWriter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.awaitResponse

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
    ).sharedUserId

    private val availablePackageInfos = mutableMapOf<String, PackageInfo>()
    private val packageInfos: MutableLiveData<List<PackageInfo>> = MutableLiveData()

    val observePackageInfos: LiveData<List<PackageInfo>> = packageInfos

    /**
     * Finds all available applications from GeoNature.
     */
    @SuppressLint("DefaultLocale")
    suspend fun getAvailableApplications(): List<PackageInfo> =
        withContext(IO) {
            availablePackageInfos.clear()

            val availableAppPackages = try {
                val geoNatureAPIClient = GeoNatureAPIClient.instance(applicationContext)
                val response = geoNatureAPIClient
                    ?.getApplications()
                    ?.awaitResponse()

                if (response?.isSuccessful == true) {
                    response.body()
                        ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }

            availableAppPackages
                .asSequence()
                .map {
                    PackageInfo(
                        it.packageName,
                        it.code
                            .toLowerCase()
                            .capitalize(),
                        it.versionCode.toLong(),
                        0,
                        null,
                        it.apkUrl
                    ).apply {
                        settings = it.settings
                    }
                }
                .onEach {
                    availablePackageInfos[it.packageName] = it
                }
                .toList()
                .also {
                    getInstalledApplications()
                }
        }

    /**
     * Finds all compatible installed applications.
     */
    suspend fun getInstalledApplications(): List<PackageInfo> =
        withContext(IO) {
            val allPackageInfos = mutableMapOf<String, PackageInfo>()
            allPackageInfos.putAll(availablePackageInfos)

            pm
                .getInstalledApplications(PackageManager.GET_META_DATA)
                .asFlow()
                .filter { it.packageName.startsWith(sharedUserId) }
                .map {
                    val packageInfoFromPackageManager = pm.getPackageInfo(
                        it.packageName,
                        PackageManager.GET_META_DATA
                    )

                    val existingPackageInfo = availablePackageInfos[it.packageName]

                    @Suppress("DEPRECATION") PackageInfo(
                        it.packageName,
                        pm
                            .getApplicationLabel(it)
                            .toString(),
                        existingPackageInfo?.versionCode
                            ?: 0,
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) packageInfoFromPackageManager.longVersionCode
                        else packageInfoFromPackageManager.versionCode.toLong(),
                        packageInfoFromPackageManager.versionName,
                        existingPackageInfo?.apkUrl,
                        pm.getApplicationIcon(it.packageName),
                        pm.getLaunchIntentForPackage(it.packageName)
                    ).apply {
                        inputs = getInputsToSynchronize(this).size
                        settings = existingPackageInfo?.settings
                    }
                }
                .onEach {
                    allPackageInfos[it.packageName] = it
                }
                .toList()
                .also {
                    packageInfos.postValue(allPackageInfos.values.toList())
                }
        }

    /**
     * Gets related info from package name.
     */
    fun getPackageInfo(packageName: String): PackageInfo? {
        return availablePackageInfos[packageName]
    }

    /**
     * Fetch all available inputs to synchronize from given [PackageInfo].
     */
    suspend fun getInputsToSynchronize(packageInfo: PackageInfo): List<SyncInput> =
        withContext(IO) {
            FileUtils
                .getInputsFolder(
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

    suspend fun updateAppSettings(packageInfo: PackageInfo) =
        withContext(IO) {
            val result = runCatching { AppSettingsJsonWriter(applicationContext).write(packageInfo) }

            if (result.isFailure) {
                Log.w(
                    TAG,
                    "failed to update settings for '${packageInfo.packageName}'"
                )
            }
        }

    companion object {
        private val TAG = PackageInfoManager::class.java.name

        @Volatile
        private var INSTANCE: PackageInfoManager? = null

        /**
         * Gets the singleton instance of [PackageInfoManager].
         *
         * @param applicationContext The main application context.
         *
         * @return The singleton instance of [PackageInfoManager].
         */
        fun getInstance(applicationContext: Context): PackageInfoManager =
            INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: PackageInfoManager(applicationContext).also { INSTANCE = it }
                }
    }
}
