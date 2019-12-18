package fr.geonature.sync.api.model

import com.google.gson.annotations.SerializedName

/**
 * GeoNature User definition.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
open class User(
    @SerializedName("id_role")
    val id: Long,

    @SerializedName("nom_role")
    val lastname: String,

    @SerializedName("prenom_role")
    val firstname: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (id != other.id) return false
        if (lastname != other.lastname) return false
        if (firstname != other.firstname) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + lastname.hashCode()
        result = 31 * result + firstname.hashCode()

        return result
    }
}