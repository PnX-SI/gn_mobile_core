package fr.geonature.datasync.features.settings.usecase

import android.app.Application
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Left
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.getOrHandle
import fr.geonature.commons.fp.orNull
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.packageinfo.IPackageInfoRepository
import fr.geonature.datasync.packageinfo.PackageInfo
import fr.geonature.datasync.packageinfo.error.PackageInfoNotFoundFailure
import fr.geonature.datasync.settings.IDataSyncSettingsRepository
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundFailure
import kotlinx.coroutines.flow.firstOrNull
import org.tinylog.Logger
import javax.inject.Inject

/**
 * Check and update app settings.
 *
 * @author S. Grimault
 */
class UpdateSettingsFromRemoteUseCase @Inject constructor(
    private val application: Application,
    private val geoNatureAPIClient: IGeoNatureAPIClient,
    private val dataSyncSettingsRepository: IDataSyncSettingsRepository,
    private val packageInfoRepository: IPackageInfoRepository,
) : BaseUseCase<PackageInfo, BaseUseCase.None>() {
    override suspend fun run(params: None): Either<Failure, PackageInfo> {
        Logger.info { "loading app configuration..." }

        // loading current app configuration...
        val dataSyncSettingsResponse = dataSyncSettingsRepository.getDataSyncSettings()

        if (dataSyncSettingsResponse.isLeft) {
            return Left(dataSyncSettingsResponse.getOrHandle { it } as Failure)
        }

        val dataSyncSettings = dataSyncSettingsResponse.orNull()
            ?: return Left(DataSyncSettingsNotFoundFailure())

        geoNatureAPIClient.setBaseUrls(
            IGeoNatureAPIClient.ServerUrls(
                geoNatureBaseUrl = dataSyncSettings.geoNatureServerUrl,
                taxHubBaseUrl = dataSyncSettings.taxHubServerUrl
            )
        )

        Logger.info { "updating app configuration from '${dataSyncSettings.geoNatureServerUrl}'..." }

        // tries to gets all applications installed locally and available remotely
        val packageInfoList = packageInfoRepository.getAllApplications().firstOrNull()
            ?: emptyList()

        if (packageInfoList.isEmpty()) {
            return Left(PackageInfoNotFoundFailure(packageName = application.packageName))
        }

        val packageInfoFound =
            packageInfoList.firstOrNull { it.packageName == application.packageName }

        if (packageInfoFound == null) {
            Logger.error { "package info '${application.packageName}' not found: abort" }

            return Left(PackageInfoNotFoundFailure(packageName = application.packageName))
        }

        if (packageInfoFound.apkUrl.isNullOrBlank() && packageInfoFound.settings == null) {
            Logger.warn { "failed to update app configuration from '${dataSyncSettings.geoNatureServerUrl}'" }

            return Right(packageInfoFound)
        }

        // save locally app settings
        packageInfoRepository.updateAppSettings(packageInfoFound)

        // loading updated app configuration...
        val dataSyncSettingsUpdatedResponse = dataSyncSettingsRepository.getDataSyncSettings()

        if (dataSyncSettingsUpdatedResponse.isLeft) {
            return Left(
                when (val failure = dataSyncSettingsUpdatedResponse.getOrHandle { it } as Failure) {
                    is DataSyncSettingsNotFoundFailure -> {
                        Logger.error { "failed to load app settings${if (failure.source.isNullOrBlank()) "" else " (source: ${failure.source})"}: abort" }
                        failure
                    }
                    else -> {
                        Logger.error { "failed to load app settings: abort" }
                        failure
                    }
                },
            )
        }

        val dataSyncSettingsUpdated = dataSyncSettingsUpdatedResponse.orNull()
            ?: return Left(DataSyncSettingsNotFoundFailure())

        geoNatureAPIClient.setBaseUrls(
            IGeoNatureAPIClient.ServerUrls(
                geoNatureBaseUrl = dataSyncSettingsUpdated.geoNatureServerUrl,
                taxHubBaseUrl = dataSyncSettingsUpdated.taxHubServerUrl
            )
        )

        return Right(packageInfoFound)
    }
}