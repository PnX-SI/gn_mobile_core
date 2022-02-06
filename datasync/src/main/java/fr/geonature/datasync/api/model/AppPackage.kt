package fr.geonature.datasync.api.model

import com.google.gson.annotations.SerializedName

/**
 * Application package.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class AppPackage(
    @SerializedName("url_apk")
    val apkUrl: String,

    @SerializedName("package")
    val packageName: String,

    @SerializedName("app_code")
    val code: String,

    @SerializedName("version_code")
    val versionCode: Int,

    val settings: Any
)
