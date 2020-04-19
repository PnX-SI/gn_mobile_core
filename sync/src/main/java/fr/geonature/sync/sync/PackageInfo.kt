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
    val versionName: String,
    val icon: Drawable,
    val launchIntent: Intent?
) {
    var inputs: Int = 0
    var state: WorkInfo.State = WorkInfo.State.ENQUEUED
}
