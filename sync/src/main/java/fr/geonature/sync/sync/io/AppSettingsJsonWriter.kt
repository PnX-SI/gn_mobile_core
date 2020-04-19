package fr.geonature.sync.sync.io

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.FileUtils.getFile
import fr.geonature.mountpoint.util.FileUtils.getRootFolder
import fr.geonature.sync.api.model.AppPackage
import java.io.FileOutputStream
import java.io.FileWriter

/**
 * Default `JsonWriter` about writing app settings as `JSON` from given [AppPackage].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSettingsJsonWriter(private val context: Context) {

    fun write(appPackage: AppPackage) {
        val appSettingsFile = getFile(
            getRootFolder(
                context,
                MountPoint.StorageType.INTERNAL,
                appPackage.packageName
            ),
            "settings_${appPackage.packageName.substring(appPackage.packageName.lastIndexOf('.') + 1)}.json"
        )
        val writer = FileWriter(appSettingsFile)

        GsonBuilder().setPrettyPrinting()
            .create()
            .toJson(
                appPackage.settings,
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