package fr.geonature.sync.api.model

import com.google.gson.annotations.SerializedName

/**
 * GeoNature User definition.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class User(
    @SerializedName("id_role")
    val id: Long,
    @SerializedName("nom_role")
    val lastname: String,
    @SerializedName("prenom_role")
    val firstname: String)