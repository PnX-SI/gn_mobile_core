package fr.geonature.sync.sync

import androidx.work.WorkInfo
import fr.geonature.sync.api.model.AppPackage

/**
 * Describes [AppPackage] inputs status.
 *
 * @author S. Grimault
 */
data class AppPackageInputsStatus(
    val packageName: String,
    val state: WorkInfo.State = WorkInfo.State.ENQUEUED,
    val inputs: Int = 0
)
