package fr.geonature.datasync.packageinfo.io

import com.google.gson.GsonBuilder
import fr.geonature.datasync.packageinfo.PackageInfo
import fr.geonature.datasync.packageinfo.error.PackageInfoException
import org.tinylog.Logger
import java.io.IOException
import java.io.StringWriter

/**
 * Default `JsonWriter` about writing app settings as `JSON` from given [PackageInfo].
 *
 * @author S. Grimault
 */
class AppSettingsJsonWriter {

    @Throws(IOException::class)
    fun write(packageInfo: PackageInfo): String {
        if (packageInfo.settings == null) {
            Logger.warn { "undefined app settings to update from '${packageInfo.packageName}'" }

            throw PackageInfoException.MissingSettingsException(packageInfo.packageName)
        }

        val writer = StringWriter()

        GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(
                packageInfo.settings,
                writer
            )
        writer.flush()
        writer.close()

        return writer.toString()
    }
}
