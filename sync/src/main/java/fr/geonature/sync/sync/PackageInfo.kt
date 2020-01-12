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
    val versionName: String,
    val icon: Drawable,
    val launchIntent: Intent?
) {
    val inputs: MutableList<SyncInput> = mutableListOf()
    var state: WorkInfo.State = WorkInfo.State.ENQUEUED
}
