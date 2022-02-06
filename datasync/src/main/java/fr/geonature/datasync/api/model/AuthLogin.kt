package fr.geonature.datasync.api.model

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Authenticated user login.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class AuthLogin(
    @SerializedName("user")
    val user: AuthUser,
    @SerializedName("expires")
    val expires: Date
)
