package fr.geonature.datasync.packageinfo

import android.annotation.SuppressLint
import android.content.Context
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Left
import fr.geonature.datasync.R
import fr.geonature.datasync.packageinfo.error.NoPackageInfoFoundFromRemoteFailure
import fr.geonature.datasync.packageinfo.io.AppSettingsJsonWriter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import java.io.IOException
import java.net.UnknownHostException

/**
 * Default implementation of [IPackageInfoRepository].
 *
 * @author S. Grimault
 */
class PackageInfoRepositoryImpl(
    private val applicationContext: Context,
    private val availablePackageInfoDataSource: IPackageInfoDataSource,
    private val installedPackageInfoDataSource: IPackageInfoDataSource,
    private val appSettingsFilename: String,
) : IPackageInfoRepository {

    private val allPackageInfos = mutableMapOf<String, PackageInfo>()

    override suspend fun getAvailableApplications(): Either<Failure, List<PackageInfo>> {
        return runCatching { availablePackageInfoDataSource.getAll() }.fold(
            onSuccess = {
                if (it.isEmpty()) Left(NoPackageInfoFoundFromRemoteFailure) else Either.Right(it)
            },
            onFailure = {
                when (it) {
                    is UnknownHostException -> Left(Failure.NetworkFailure(applicationContext.getString(R.string.error_server_unreachable)))
                    is IOException -> Left(Failure.NetworkFailure(applicationContext.getString(R.string.error_network_lost)))
                    else -> Left(Failure.ServerFailure)
                }
            },
        )
    }

    override suspend fun getAllApplications(): List<PackageInfo> {
        val installedApplications = getInstalledApplications()
        val availableApplications = runCatching {
            availablePackageInfoDataSource.getAll()
        }.getOrNull()
            ?: emptyList()

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

        return allPackageInfos.values.toList()
    }

    @SuppressLint("QueryPermissionsNeeded")
    override suspend fun getInstalledApplications(): List<PackageInfo> {
        val installedApplications = runCatching {
            installedPackageInfoDataSource.getAll()
        }.getOrNull()
            ?: emptyList()

        if (installedApplications.isEmpty()) {
            return installedApplications
        }

        return installedApplications.map { packageInfo ->
            packageInfo.apply {
                settings = allPackageInfos[packageInfo.packageName]?.settings
            }
        }
    }

    override suspend fun getPackageInfo(packageName: String): PackageInfo? {
        return allPackageInfos[packageName]
    }

    override suspend fun getInputsToSynchronize(packageInfo: PackageInfo): List<SyncInput> {
        return packageInfo.getInputsToSynchronize(applicationContext)
    }

    override suspend fun updateAppSettings(packageInfo: PackageInfo) =
        withContext(IO) {
            Logger.info { "updating settings for '${packageInfo.packageName}'..." }

            val result = runCatching {
                AppSettingsJsonWriter(
                    applicationContext,
                    appSettingsFilename
                ).write(packageInfo)
            }

            if (result.isFailure) {
                Logger.warn { "failed to update settings for '${packageInfo.packageName}'" }
            }
        }
}
