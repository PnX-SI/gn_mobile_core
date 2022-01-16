package fr.geonature.sync.sync

import androidx.work.WorkInfo
import fr.geonature.datasync.api.model.AppPackage

/**
 * Describes [AppPackage] download status.
 *
 * @author S. Grimault
 */
data class AppPackageDownloadStatus(
    val packageName: String,
    val state: WorkInfo.State,
    val progress: Int = -1,
    val apkFilePath: String? = null
)
