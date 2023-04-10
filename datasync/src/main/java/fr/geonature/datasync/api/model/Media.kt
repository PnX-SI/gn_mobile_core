package fr.geonature.datasync.api.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import fr.geonature.compat.os.readSerializableCompat
import java.util.Date
import java.util.UUID

/**
 * Media added/uploaded from _GeoNature_.
 *
 * @author S. Grimault
 */
data class Media(
    @SerializedName("id_media") val id: Int,
    @SerializedName("unique_id_media") val uuid: UUID,
    @SerializedName("id_nomenclature_media_type") val mediaType: Int,
    @SerializedName("id_table_location") val idTableLocation: Int,
    @SerializedName("media_path") val path: String,
    @SerializedName("author") val author: String,
    @SerializedName("title_en") val titleEn: String?,
    @SerializedName("title_fr") val titleFr: String?,
    @SerializedName("description_en") val descriptionEn: String?,
    @SerializedName("description_fr") val descriptionFr: String?,
    @SerializedName("meta_create_date") val createdAt: Date,
    @SerializedName("meta_update_date") val updatedAt: Date
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readInt(),
        source.readSerializableCompat()
            ?: UUID.randomUUID(),
        source.readInt(),
        source.readInt(),
        source.readString()!!,
        source.readString()!!,
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readSerializableCompat()
            ?: Date(),
        source.readSerializableCompat()
            ?: Date()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.also {
            it.writeInt(id)
            it.writeSerializable(uuid)
            it.writeInt(mediaType)
            it.writeInt(idTableLocation)
            it.writeString(path)
            it.writeString(author)
            it.writeString(titleEn)
            it.writeString(titleFr)
            it.writeString(descriptionEn)
            it.writeString(descriptionFr)
            it.writeSerializable(createdAt)
            it.writeSerializable(updatedAt)
        }
    }

    companion object CREATOR : Parcelable.Creator<Media> {

        override fun createFromParcel(source: Parcel): Media {
            return Media(source)
        }

        override fun newArray(size: Int): Array<Media?> {
            return arrayOfNulls(size)
        }
    }
}
