package fr.geonature.datasync.packageinfo

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import fr.geonature.datasync.packageinfo.io.AppSettingsJsonWriter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Default implementation of [IPackageInfoRepository].
 *
 * @author S. Grimault
 */
class PackageInfoRepositoryImpl(
    private val applicationContext: Context,
    private val availablePackageInfoDataSource: IPackageInfoDataSource,
    private val installedPackageInfoDataSource: IPackageInfoDataSource
) : IPackageInfoRepository {

    private val allPackageInfos = mutableMapOf<String, PackageInfo>()

    override fun getAllApplications(): Flow<List<PackageInfo>> = flow {
        val installedApplications = getInstalledApplications().firstOrNull()
            ?: emptyList()
        val availableApplications = availablePackageInfoDataSource
            .getAll()
            .firstOrNull()
            ?: emptyList()

        emit(installedApplications)

        allPackageInfos.clear()
        allPackageInfos.putAll((installedApplications
            .associateBy { it.packageName }
            .asSequence() + availableApplications
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
        val installedApplications = installedPackageInfoDataSource
            .getAll()
            .firstOrNull()
            ?: emptyList()

        if (installedApplications.isEmpty()) {
            emit(installedApplications)

            return@flow
        }

        emit(installedApplications.map { packageInfo ->
            packageInfo.apply {
                settings = allPackageInfos[packageInfo.packageName]?.settings
            }
        })
    }

    override suspend fun getPackageInfo(packageName: String): PackageInfo? {
        return allPackageInfos[packageName]
    }

    override suspend fun getInputsToSynchronize(packageInfo: PackageInfo): List<SyncInput> {
        return packageInfo.getInputsToSynchronize(applicationContext)
    }

    override suspend fun updateAppSettings(packageInfo: PackageInfo) = withContext(IO) {
        Log.i(
            TAG,
            "updating settings for '${packageInfo.packageName}'..."
        )
        
        val result = runCatching { AppSettingsJsonWriter(applicationContext).write(packageInfo) }

        if (result.isFailure) {
            Log.w(
                TAG,
                "failed to update settings for '${packageInfo.packageName}'"
            )
        }
    }

    companion object {
        private val TAG = PackageInfoRepositoryImpl::class.java.name
    }
}
