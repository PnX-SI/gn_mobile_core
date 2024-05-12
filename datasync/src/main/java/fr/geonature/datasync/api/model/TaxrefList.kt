package fr.geonature.datasync.api.model

import com.google.gson.annotations.SerializedName

/**
 * _GeoNature_ taxa list result.
 *
 * @author S. Grimault
 */
data class TaxrefListListResult(
    val data: List<TaxrefList>,
    val count: Int
)


/**
 * _GeoNature_ taxa list definition.
 *
 * @author S. Grimault
 */
data class TaxrefList(
    @SerializedName("id_liste") val id: Long,
    @SerializedName("code_liste") val code: String,
    @SerializedName("nom_liste") val name: String,
    @SerializedName("desc_liste") val description: String
)
