package fr.geonature.datasync.packageinfo.io

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import fr.geonature.datasync.packageinfo.PackageInfo
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.FileUtils.getFile
import fr.geonature.mountpoint.util.FileUtils.getRootFolder
import java.io.FileWriter
import java.io.IOException

/**
 * Default `JsonWriter` about writing app settings as `JSON` from given [PackageInfo].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSettingsJsonWriter(private val context: Context) {

    @Throws(IOException::class)
    fun write(packageInfo: PackageInfo) {
        if (packageInfo.settings == null) {
            Log.w(
                TAG,
                "undefined app settings to update from '${packageInfo.packageName}'"
            )

            return
        }
        
        val appRootFolder = getRootFolder(
            context,
            MountPoint.StorageType.INTERNAL
        ).also { it.mkdirs() }

        val appSettingsFile = getFile(
            appRootFolder,
            "settings_${packageInfo.packageName.substring(packageInfo.packageName.lastIndexOf('.') + 1)}.json"
        )
        val writer = FileWriter(appSettingsFile)

        GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(
                packageInfo.settings,
                writer
            )
        writer.flush()
        writer.close()

        Log.i(
            TAG,
            "updating app settings '${appSettingsFile.absolutePath}'"
        )
    }

    companion object {
        private val TAG = AppSettingsJsonWriter::class.java.name
    }
}
