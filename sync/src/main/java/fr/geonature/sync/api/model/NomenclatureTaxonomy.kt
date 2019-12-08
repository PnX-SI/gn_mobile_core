package fr.geonature.sync.api.model

import com.google.gson.annotations.SerializedName

/**
 * GeoNature nomenclature type taxonomy rank definition.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class NomenclatureTaxonomy(

    @SerializedName("regne")
    val kingdom: String,

    @SerializedName("group2_inpn")
    val group: String)