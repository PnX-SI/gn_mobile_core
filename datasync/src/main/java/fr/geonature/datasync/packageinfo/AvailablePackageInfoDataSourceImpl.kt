package fr.geonature.datasync.packageinfo

import fr.geonature.datasync.api.IGeoNatureAPIClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse
import java.util.Locale

/**
 * Gets available [PackageInfo] from GeoNature.
 */
class AvailablePackageInfoDataSourceImpl(private val geoNatureAPIClient: IGeoNatureAPIClient) :
    IPackageInfoDataSource {

    override fun getAll(): Flow<List<PackageInfo>> = flow {
        emit(runCatching {
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
            .getOrElse { emptyList() })
    }
}