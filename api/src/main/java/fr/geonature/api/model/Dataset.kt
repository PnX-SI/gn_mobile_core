package fr.geonature.api.model

import com.google.gson.annotations.SerializedName

/**
 * Dataset query.
 *
 * @author S. Grimault
 */
data class DatasetQuery(
    @SerializedName("create") val code: String
)
