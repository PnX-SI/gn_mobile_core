package fr.geonature.datasync.api.model

import com.google.gson.annotations.SerializedName

/**
 * _GeoNature_ Taxa list result.
 *
 * @author S. Grimault
 */
data class TaxrefListResult(
    val items: List<Taxref>,
    val total: Int,
    val limit: Int,
    val page: Int
)

/**
 * _GeoNature_ Taxref definition.
 *
 * @author S. Grimault
 */
data class Taxref(
    @SerializedName("cd_nom")
    val id: Long,

    @SerializedName("cd_ref")
    val ref: Long,

    @SerializedName("lb_nom")
    val name: String,

    @SerializedName("nom_valide")
    val fullName: String?,

    @SerializedName("nom_vern")
    val commonName: String?,

    @SerializedName("nom_complet")
    val description: String,

    @SerializedName("regne")
    val kingdom: String?,

    @SerializedName("group2_inpn")
    val group: String?,

    @SerializedName("listes")
    val list: List<Long>?
)
