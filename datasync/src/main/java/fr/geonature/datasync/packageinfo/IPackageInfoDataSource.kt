package fr.geonature.datasync.packageinfo

import kotlinx.coroutines.flow.Flow

/**
 * [PackageInfo] data source.
 *
 * @author S. Grimault
 */
interface IPackageInfoDataSource {

    /**
     * Gets all [PackageInfo] from this data source.
     */
    fun getAll(): Flow<List<PackageInfo>>
}