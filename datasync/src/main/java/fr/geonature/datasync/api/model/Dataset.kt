package fr.geonature.datasync.api.model

import com.google.gson.annotations.SerializedName

/**
 * Dataset query.
 *
 * @author S. Grimault
 */
data class DatasetQuery(
    @SerializedName("create") val code: String
)
