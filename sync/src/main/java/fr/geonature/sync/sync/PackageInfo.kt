package fr.geonature.sync.sync

import android.content.Intent
import android.graphics.drawable.Drawable

/**
 * Describes the contents of a package.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class PackageInfo(
    val packageName: String,
    val label: String,
    val versionCode: Long,
    val localVersionCode: Long = 0,
    val versionName: String? = null,
    val apkUrl: String? = null,
    val icon: Drawable? = null,
    val launchIntent: Intent? = null
) : Comparable<PackageInfo> {
    var settings: Any? = null
    var inputsStatus: AppPackageInputsStatus? = null
    var downloadStatus: AppPackageDownloadStatus? = null

    override fun compareTo(other: PackageInfo): Int {
        return packageName.compareTo(other.packageName)
    }

    fun isAvailableForInstall(): Boolean {
        return localVersionCode == 0L && !apkUrl.isNullOrEmpty()
    }

    fun hasNewVersionAvailable(): Boolean {
        return versionCode > localVersionCode && !apkUrl.isNullOrEmpty()
    }
}
