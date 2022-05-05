package fr.geonature.datasync.api.model

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * GeoNature Taxref with area definition.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class TaxrefArea(
    @SerializedName("cd_nom")
    val taxrefId: Long,

    @SerializedName("id_area")
    val areaId: Long,

    @SerializedName("color")
    val color: String,

    @SerializedName("nb_obs")
    val numberOfObservers: Int,

    @SerializedName("last_date")
    val lastUpdatedAt: Date
)
