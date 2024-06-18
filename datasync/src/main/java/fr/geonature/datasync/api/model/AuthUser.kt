package fr.geonature.datasync.api.model

import com.google.gson.annotations.SerializedName

/**
 * Authenticated user.
 *
 * @author S. Grimault
 */
class AuthUser(
    id: Long,
    lastname: String,
    firstname: String?,

    @SerializedName("id_application")
    val applicationId: Int,

    @SerializedName("id_organisme")
    val organismId: Int,

    @SerializedName("identifiant")
    val login: String
) : User(
    id,
    lastname,
    firstname
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AuthUser) return false
        if (!super.equals(other)) return false

        if (applicationId != other.applicationId) return false
        if (organismId != other.organismId) return false
        if (login != other.login) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + applicationId
        result = 31 * result + organismId
        result = 31 * result + login.hashCode()

        return result
    }
}
