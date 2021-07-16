package fr.geonature.sync.sync

import kotlinx.coroutines.flow.Flow

/**
 * [PackageInfo] manager.
 *
 * Retrieves various kinds of information related to the application packages that are currently
 * installed on the device.
 *
 * @author S. Grimault
 */
interface IPackageInfoManager {

    /**
     * Gets all available applications.
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
    suspend fun getInputsToSynchronize(packageInfo: PackageInfo): List<SyncInput>

    /**
     * Updates local settings from given [PackageInfo].
     */
    suspend fun updateAppSettings(packageInfo: PackageInfo)
}