package fr.geonature.datasync.packageinfo.io

import com.google.gson.Gson
import fr.geonature.datasync.api.model.AppPackage
import org.json.JSONArray
import org.json.JSONTokener

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding [AppPackage].
 *
 * @author S. Grimault
 */
class AppPackageJsonReader {

    /**
     * parse a `JSON` string to convert as list of [AppPackage].
     *
     * @param json the `JSON` string to parse
     * @return a list of [AppPackage] instances from the `JSON` string or empty if something goes wrong
     */
    fun read(json: String?): List<AppPackage> {
        if (json.isNullOrBlank()) {
            return emptyList()
        }

        val nextValue = JSONTokener(json).nextValue()

        return (if (nextValue is JSONArray) {
            Gson()
                .fromJson(
                    json,
                    Array<AppPackage>::class.java
                )
                .toList()
        } else {
            listOf(
                Gson().fromJson(
                    json,
                    AppPackage::class.java
                )
            )
        }).filter {
            @Suppress("SENSELESS_COMPARISON", "SAFE_CALL_WILL_CHANGE_NULLABILITY")
            it.packageName != null && it.packageName.isNotBlank() && it.apkUrl != null && it.apkUrl?.isNotBlank()
        }
    }
}