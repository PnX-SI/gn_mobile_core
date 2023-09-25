package fr.geonature.datasync.api.model

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Taxref version metadata.
 *
 * @author S. Grimault
 */
data class TaxrefVersion(
    @SerializedName("referencial_name") val referentialName: String,
    @SerializedName("version") val version: Int,
    @SerializedName("update_date") val updatedAt: Date
)
