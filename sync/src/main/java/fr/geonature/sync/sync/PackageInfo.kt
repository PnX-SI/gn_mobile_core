package fr.geonature.sync.sync

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.work.WorkInfo

/**
 * Describes the contents of a package.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class PackageInfo(
    val packageName: String,
    val label: String,
    val versionCode: Long,
    val versionName: String? = null,
    val icon: Drawable? = null,
    val launchIntent: Intent? = null
): Comparable<PackageInfo> {
    var inputs: Int = 0
    var state: WorkInfo.State = WorkInfo.State.ENQUEUED
    var apk: String? = null
    var settings: Any? = null

    override fun compareTo(other: PackageInfo): Int {
        return packageName.compareTo(other.packageName)
    }
}
