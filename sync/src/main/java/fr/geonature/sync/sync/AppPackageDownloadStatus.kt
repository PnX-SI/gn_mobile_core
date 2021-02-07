package fr.geonature.sync.sync

import androidx.work.WorkInfo
import fr.geonature.sync.api.model.AppPackage

/**
 * Describes [AppPackage] download status.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class AppPackageDownloadStatus(
    val state: WorkInfo.State,
    val packageName: String,
    val progress: Int = -1,
    val apkFilePath: String? = null
)
