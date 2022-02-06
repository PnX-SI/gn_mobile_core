package fr.geonature.datasync.api.model

import com.google.gson.annotations.SerializedName

/**
 * GeoNature nomenclature type definition.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class NomenclatureType(
    @SerializedName("id_type")
    val id: Long,

    @SerializedName("mnemonique")
    val mnemonic: String,

    @SerializedName("label_default")
    val defaultLabel: String,

    @SerializedName("nomenclatures")
    val nomenclatures: List<Nomenclature>
)
