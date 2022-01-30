package fr.geonature.datasync.packageinfo

import org.json.JSONObject

/**
 * Describes an input to synchronize.
 *
 * @author S. Grimault
 */
data class SyncInput(
    val filePath: String,
    val module: String,
    val payload: JSONObject
)
