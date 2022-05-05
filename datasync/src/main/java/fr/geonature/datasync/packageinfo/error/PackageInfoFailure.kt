package fr.geonature.datasync.packageinfo.error

import fr.geonature.commons.error.Failure
import fr.geonature.datasync.packageinfo.PackageInfo

/**
 * Failure about no [PackageInfo] found from GeoNature.
 *
 * @author S. Grimault
 */
object NoPackageInfoFoundFromRemoteFailure : Failure.FeatureFailure()

/**
 * Failure about no [PackageInfo] found locally.
 */
object NoPackageInfoFoundFailure : Failure.FeatureFailure()

/**
 * Failure about [PackageInfo] not found from GeoNature.
 *
 * @author S. Grimault
 */
data class PackageInfoNotFoundFromRemoteFailure(val packageName: String) : Failure.FeatureFailure()

/**
 * Failure about [PackageInfo] not found locally.
 *
 * @author S. Grimault
 */
data class PackageInfoNotFoundFailure(val packageName: String) : Failure.FeatureFailure()