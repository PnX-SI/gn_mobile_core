package fr.geonature.sync.sync

import android.content.Intent
import android.graphics.drawable.Drawable

/**
 * Describes the contents of a package.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class PackageInfo(val packageName: String,
                       val label: String,
                       val versionName: String,
                       val icon: Drawable,
                       val inputs: Int,
                       val launchIntent: Intent?)