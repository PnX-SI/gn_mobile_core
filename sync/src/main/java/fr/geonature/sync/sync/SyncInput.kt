package fr.geonature.sync.sync

import org.json.JSONObject

/**
 * Describes an input to synchronize.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class SyncInput(val packageInfo: PackageInfo,
                     val filePath: String,
                     val module: String,
                     val payload: JSONObject)