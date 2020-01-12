package fr.geonature.sync.api.model

import com.google.gson.annotations.SerializedName

/**
 * Authentication login credentials.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class AuthCredentials(
    @SerializedName("login")
    val login: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("id_application")
    val applicationId: Int
)
