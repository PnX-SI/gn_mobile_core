package fr.geonature.datasync.packageinfo

import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import kotlinx.coroutines.flow.Flow

/**
 * [PackageInfo] manager.
 *
 * Retrieves various kinds of information related to the application packages that are currently
 * installed on the device.
 *
 * @author S. Grimault
 */
interface IPackageInfoRepository {

    /**
     * Gets all applications available remotely.
     */
    suspend fun getAvailableApplications(): Either<Failure, List<PackageInfo>>

    /**
     * Gets all applications installed locally and available remotely.
     */
    fun getAllApplications(): Flow<List<PackageInfo>>

    /**
     * Gets all compatible installed applications.
     */
    fun getInstalledApplications(): Flow<List<PackageInfo>>

    /**
     * Gets related info from package name.
     */
    suspend fun getPackageInfo(packageName: String): PackageInfo?

    /**
     * Fetch all available inputs to synchronize from given [PackageInfo].
     */
    @Deprecated("use directly getInputsToSynchronize() from PackageInfo")
    suspend fun getInputsToSynchronize(packageInfo: PackageInfo): List<SyncInput>

    /**
     * Updates local settings from given [PackageInfo].
     */
    suspend fun updateAppSettings(packageInfo: PackageInfo)
}