package fr.geonature.datasync.api.model

import com.google.gson.annotations.SerializedName

/**
 * Login response errorResourceId.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class AuthLoginError(
    @SerializedName("type")
    val type: String,
    @SerializedName("msg")
    val message: String
)
