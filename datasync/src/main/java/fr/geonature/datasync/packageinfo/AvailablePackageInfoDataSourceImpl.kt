package fr.geonature.datasync.packageinfo

import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.packageinfo.io.AppPackageJsonReader
import retrofit2.awaitResponse
import java.util.Locale

/**
 * Gets available [PackageInfo] from GeoNature.
 */
class AvailablePackageInfoDataSourceImpl(private val geoNatureAPIClient: IGeoNatureAPIClient) :
    IPackageInfoDataSource {

    override suspend fun getAll(): List<PackageInfo> {
        return runCatching {
            geoNatureAPIClient
                .getApplications()
                .awaitResponse()
        }
            .map {
                if (it.isSuccessful) AppPackageJsonReader().read(it
                    .body()
                    ?.byteStream()
                    ?.bufferedReader()
                    ?.readText())
                else emptyList()
            }
            .map { appPackages ->
                appPackages
                    .asSequence()
                    .map {
                        PackageInfo(it.packageName,
                            it.code
                                .lowercase(Locale.ROOT)
                                .replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.ROOT) else c.toString() },
                            it.versionCode.toLong(),
                            0,
                            null,
                            it.apkUrl).apply {
                            settings = it.settings
                        }
                    }
                    .toList()
            }
            .getOrThrow()
    }
}