package fr.geonature.datasync.packageinfo

/**
 * [PackageInfo] data source.
 *
 * @author S. Grimault
 */
interface IPackageInfoDataSource {

    /**
     * Gets all [PackageInfo] from this data source.
     */
    suspend fun getAll(): List<PackageInfo>
}