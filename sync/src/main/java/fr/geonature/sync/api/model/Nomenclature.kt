package fr.geonature.sync.api.model

import com.google.gson.annotations.SerializedName

/**
 * GeoNature nomenclature item definition.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class Nomenclature(
    @SerializedName("id_nomenclature")
    val id: Long,

    @SerializedName("cd_nomenclature")
    val code: String,

    @SerializedName("hierarchy")
    val hierarchy: String?,

    @SerializedName("label_default")
    val defaultLabel: String,

    val taxref: List<NomenclatureTaxonomy>
)
