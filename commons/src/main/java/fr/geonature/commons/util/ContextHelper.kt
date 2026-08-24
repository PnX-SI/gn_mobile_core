package fr.geonature.commons.util

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * Function helpers for `Context`.
 *
 * @author S. Grimault
 */

/**
 * Gets the primary external storage for app directory from the given context.
 *
 * Checks that the mount point is available (i.e. [Environment.MEDIA_MOUNTED]) before
 * accessing [Context.getExternalFilesDir] or [Environment.getExternalStorageDirectory].
 * Falls back to [Context.filesDir] if no external storage is available.
 */
fun Context.getPrimaryExternalStorage(): File {
    // we have something like that: /storage/emulated/0/Android/data/fr.geonature.sync/files
    val externalFilesDir = getExternalFilesDir(null)

    if (externalFilesDir != null && Environment.getExternalStorageState(externalFilesDir) == Environment.MEDIA_MOUNTED) {
        return externalFilesDir.absolutePath
            .removeSuffix("/${packageName}/files")
            .let { File(it).getFile(packageName) }
    }

    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        // we have something like that: /storage/emulated/0
        return Environment.getExternalStorageDirectory().absolutePath
            .removeSuffix("/${packageName}/files")
            .removeSuffix("/Android/data")
            .let {
                File(it).getFile(
                    "Android",
                    "data",
                    packageName
                )
            }
    }

    // we have something like that: /data/user/0/fr.geonature.sync/files or /data/data/fr.geonature.sync/files
    return filesDir.absolutePath
        .removeSuffix("/${packageName}/files")
        .let { File(it).getFile(packageName) }
}
