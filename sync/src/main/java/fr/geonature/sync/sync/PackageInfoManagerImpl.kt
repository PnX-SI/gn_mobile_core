package fr.geonature.sync.sync

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.WorkInfo
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.mountpoint.util.FileUtils
import fr.geonature.sync.sync.io.AppSettingsJsonWriter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.awaitResponse
import java.util.Locale

/**
 * Default implementation of [IPackageInfoManager].
 *
 * @author S. Grimault
 */
class PackageInfoManagerImpl(
    private val applicationContext: Context,
    private val geoNatureAPIClient: IGeoNatureAPIClient
) : IPackageInfoManager {

    private val pm = applicationContext.packageManager
    private val sharedUserId = pm.getPackageInfo(
        applicationContext.packageName,
        PackageManager.GET_META_DATA
    ).sharedUserId

    private val allPackageInfos = mutableMapOf<String, PackageInfo>()

    override fun getAllApplications(): Flow<List<PackageInfo>> = flow {
        val installedApplications = (getInstalledApplications().firstOrNull()
            ?: emptyList())

        emit(installedApplications)

        allPackageInfos.clear()
        allPackageInfos.putAll((installedApplications
            .associateBy { it.packageName }
            .asSequence() + getAvailableApplications()
            .associateBy { it.packageName }
            .asSequence())
            .distinct()
            .groupBy({ it.key },
                { it.value })
            .mapValues {
                when (it.value.size) {
                    2 -> it.value[0]
                        .copy(
                            versionCode = it.value[1].versionCode,
                            apkUrl = it.value[1].apkUrl
                        )
                        .apply {
                            settings = it.value[1].settings
                        }
                    else -> it.value[0]
                }
            })

        emit(allPackageInfos.values.toList())
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun getInstalledApplications(): Flow<List<PackageInfo>> = flow {
        emit(withContext(IO) {
            pm
                .getInstalledApplications(PackageManager.GET_META_DATA)
                .asFlow()
                .filter { it.packageName.startsWith(sharedUserId) }
                .map {
                    val packageInfoFromPackageManager = pm.getPackageInfo(
                        it.packageName,
                        PackageManager.GET_META_DATA
                    )

                    @Suppress("DEPRECATION") PackageInfo(
                        it.packageName,
                        pm
                            .getApplicationLabel(it)
                            .toString(),
                        0,
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) packageInfoFromPackageManager.longVersionCode
                        else packageInfoFromPackageManager.versionCode.toLong(),
                        packageInfoFromPackageManager.versionName,
                        null,
                        pm.getApplicationIcon(it.packageName),
                        pm.getLaunchIntentForPackage(it.packageName)
                    ).apply {
                        inputsStatus = AppPackageInputsStatus(
                            it.packageName,
                            WorkInfo.State.ENQUEUED,
                            getInputsToSynchronize(this).size
                        )
                    }
                }
                .toList()
        })
    }

    override suspend fun getPackageInfo(packageName: String): PackageInfo? {
        return allPackageInfos[packageName]
    }

    override suspend fun getInputsToSynchronize(packageInfo: PackageInfo): List<SyncInput> =
        withContext(IO) {
            FileUtils
                .getInputsFolder(applicationContext)
                .walkTopDown()
                .filter { it.isFile && it.extension == "json" }
                .filter { it.nameWithoutExtension.startsWith("input") }
                .filter { it.nameWithoutExtension.contains(packageInfo.packageName.substringAfterLast(".")) }
                .filter { it.canRead() }
                .map {
                    val toJson = kotlin
                        .runCatching { JSONObject(it.readText()) }
                        .getOrNull()

                    if (toJson == null) {
                        Log.w(
                            TAG,
                            "invalid input file found '${it.name}'"
                        )

                        it.delete()

                        return@map null
                    }

                    val module = kotlin
                        .runCatching { toJson.getString("module") }
                        .getOrNull()

                    if (module.isNullOrBlank()) {
                        Log.w(
                            TAG,
                            "invalid input file found '${it.name}': missing 'module' attribute"
                        )

                        return@map null
                    }

                    SyncInput(
                        packageInfo,
                        it.absolutePath,
                        module,
                        toJson
                    )
                }
                .filterNotNull()
                .toList()
        }

    override suspend fun updateAppSettings(packageInfo: PackageInfo) = withContext(IO) {
        val result = runCatching { AppSettingsJsonWriter(applicationContext).write(packageInfo) }

        if (result.isFailure) {
            Log.w(
                TAG,
                "failed to update settings for '${packageInfo.packageName}'"
            )
        }
    }

    /**
     * Finds all available applications from GeoNature.
     */
    private suspend fun getAvailableApplications(): List<PackageInfo> = withContext(IO) {
        runCatching {
            geoNatureAPIClient
                .getApplications()
                .awaitResponse()
        }
            .map {
                if (it.isSuccessful) it.body()
                    ?: emptyList() else emptyList()
            }
            .map { appPackages ->
                appPackages
                    .asSequence()
                    .map {
                        PackageInfo(
                            it.packageName,
                            it.code
                                .lowercase(Locale.ROOT)
                                .replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.ROOT) else c.toString() },
                            it.versionCode.toLong(),
                            0,
                            null,
                            it.apkUrl
                        ).apply {
                            settings = it.settings
                        }
                    }
                    .toList()
            }
            .getOrElse { emptyList() }
    }

    companion object {
        private val TAG = PackageInfoManagerImpl::class.java.name
    }
}
