package fr.geonature.datasync.packageinfo

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.tinylog.Logger

/**
 * Describes the contents of an application package.
 *
 * @author S. Grimault
 */
data class PackageInfo(
    val packageName: String,
    val label: String,
    val versionCode: Long,
    val localVersionCode: Long = 0,
    val versionName: String? = null,
    val apkUrl: String? = null,
    val icon: Drawable? = null,
    val launchIntent: Intent? = null
) : Comparable<PackageInfo> {
    var settings: Any? = null
    var inputsStatus: AppPackageInputsStatus? = null
    var downloadStatus: AppPackageDownloadStatus? = null

    override fun compareTo(other: PackageInfo): Int {
        return packageName.compareTo(other.packageName)
    }

    fun isAvailableForInstall(): Boolean {
        return localVersionCode == 0L && !apkUrl.isNullOrEmpty()
    }

    fun hasNewVersionAvailable(): Boolean {
        return versionCode > localVersionCode && !apkUrl.isNullOrEmpty()
    }

    /**
     * Returns a list of available [SyncInput] ready to synchronize.
     */
    suspend fun getInputsToSynchronize(
        applicationContext: Context,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): List<SyncInput> =
        withContext(dispatcher) {
            FileUtils
                .getInputsFolder(
                    applicationContext,
                    packageName
                )
                .walkTopDown()
                .filter { it.isFile && it.extension == "json" }
                .filter { it.nameWithoutExtension.startsWith("input") }
                .filter { it.canRead() }
                .map {
                    val toJson = runCatching { JSONObject(it.readText()) }.getOrNull()

                    if (toJson == null) {
                        Logger.warn { "invalid input file found '${it.name}'" }

                        it.delete()

                        return@map null
                    }

                    val module = toJson
                        .optString("module")
                        .takeIf { module -> module.isNotBlank() }
                        ?: toJson
                            .optJSONObject("properties")
                            ?.getString("module")

                    if (module.isNullOrBlank()) {
                        Logger.warn { "invalid input file found '${it.name}': missing 'module' attribute" }

                        return@map null
                    }

                    SyncInput(
                        it.absolutePath,
                        module,
                        toJson
                    )
                }
                .filterNotNull()
                .toList()
        }
}
