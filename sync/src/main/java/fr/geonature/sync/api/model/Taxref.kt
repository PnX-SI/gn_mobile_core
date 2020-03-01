package fr.geonature.sync.api.model

import com.google.gson.annotations.SerializedName

/**
 * GeoNature Taxref definition.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class Taxref(
    @SerializedName("cd_nom")
    val id: Long,

    @SerializedName("cd_ref")
    val ref: Long,

    @SerializedName("lb_nom")
    val name: String,

    @SerializedName("search_name")
    val description: String,

    @SerializedName("regne")
    val kingdom: String,

    @SerializedName("group2_inpn")
    val group: String
)
