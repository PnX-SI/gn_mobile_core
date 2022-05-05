package fr.geonature.datasync.features.settings.usecase

import android.app.Application
import android.net.Uri
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.getOrHandle
import fr.geonature.commons.fp.map
import fr.geonature.commons.fp.orNull
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.packageinfo.IPackageInfoRepository
import fr.geonature.datasync.packageinfo.error.PackageInfoNotFoundFromRemoteFailure
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.settings.IDataSyncSettingsRepository
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundFailure
import org.tinylog.Logger
import javax.inject.Inject

/**
 * Check and get app settings from given GeoNature server URL.
 *
 * @author S. Grimault
 */
class GetAppSettingsFromRemoteUseCase @Inject constructor(
    private val application: Application,
    private val geoNatureAPIClient: IGeoNatureAPIClient,
    private val dataSyncSettingsRepository: IDataSyncSettingsRepository,
    private val packageInfoRepository: IPackageInfoRepository,
) : BaseUseCase<DataSyncSettings, String>() {

    override suspend fun run(params: String): Either<Failure, DataSyncSettings> {
        val serverUrl = "${Uri.parse(params).scheme?.run { "" } ?: "https://"}$params"

        Logger.info { "loading app configuration from '$serverUrl'..." }

        geoNatureAPIClient.setBaseUrls(IGeoNatureAPIClient.ServerUrls(geoNatureBaseUrl = serverUrl))

        // tries to fetch app settings from GeoNature
        val packageInfoFoundResponse = packageInfoRepository.getAvailableApplications()

        if (packageInfoFoundResponse.isLeft) {
            val failure = packageInfoFoundResponse.getOrHandle { it } as Failure

            when (failure) {
                is Failure.NetworkFailure -> {
                    Logger.warn { "not connected: abort" }
                }
                else -> {
                    Logger.error { "server error: abort" }
                }
            }

            return Left(failure)
        }

        val packageInfoFound = packageInfoFoundResponse
            .map { packageInfoList -> packageInfoList.firstOrNull { it.packageName == application.packageName } }
            .orNull()

        if (packageInfoFound == null) {
            Logger.error { "package info '${application.packageName}' not found from '$serverUrl': abort" }

            return Left(PackageInfoNotFoundFromRemoteFailure(packageName = application.packageName))
        }

        // save locally app settings
        packageInfoRepository.updateAppSettings(packageInfoFound)

        val dataSyncSettingsResponse = dataSyncSettingsRepository.getDataSyncSettings()

        if (dataSyncSettingsResponse.isLeft) {
            when (val failure = dataSyncSettingsResponse.getOrHandle { it } as Failure) {
                is DataSyncSettingsNotFoundFailure -> {
                    Logger.error { "failed to load app settings${if (failure.source.isNullOrBlank()) "" else " (source: ${failure.source})"}: abort" }
                }
                else -> {
                    Logger.error { "failed to load app settings: abort" }
                }
            }
        }

        val dataSyncSettings = dataSyncSettingsResponse.orNull()
            ?: return Left(DataSyncSettingsNotFoundFailure())

        geoNatureAPIClient.setBaseUrls(
            IGeoNatureAPIClient.ServerUrls(
                geoNatureBaseUrl = dataSyncSettings.geoNatureServerUrl,
                taxHubBaseUrl = dataSyncSettings.taxHubServerUrl
            )
        )

        Logger.info { "app configuration successfully loaded from '$serverUrl'" }

        // returns loaded app settings
        return Right(dataSyncSettings)
    }
}