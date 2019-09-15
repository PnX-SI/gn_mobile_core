package fr.geonature.commons.data

import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Base taxon.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
abstract class AbstractTaxon : Parcelable {

    /**
     * The unique ID of the taxon.
     */
    @ColumnInfo(name = COLUMN_ID)
    var id: Long

    /**
     * The default name of the taxon.
     */
    @ColumnInfo(name = COLUMN_NAME)
    var name: String

    @Embedded
    val taxonomy: Taxonomy

    /**
     * The description of the taxon.
     */
    @ColumnInfo(name = COLUMN_DESCRIPTION)
    var description: String?

    /**
     * Whether the taxon is part of the heritage.
     */
    @ColumnInfo(name = COLUMN_HERITAGE)
    var heritage: Boolean = false

    constructor(id: Long,
                name: String,
                taxonomy: Taxonomy,
                description: String?,
                heritage: Boolean = false) {
        this.id = id
        this.name = name
        this.taxonomy = taxonomy
        this.description = description
        this.heritage = heritage
    }

    constructor(source: Parcel) : this(source.readLong(),
                                       source.readString()!!,
                                       source.readParcelable(Taxonomy::class.java.classLoader)!!,
                                       source.readString(),
                                       source.readByte() == 1.toByte())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractTaxon) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (taxonomy != other.taxonomy) return false
        if (description != other.description) return false
        if (heritage != other.heritage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + taxonomy.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + heritage.hashCode()
        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeLong(id)
        dest?.writeString(name)
        dest?.writeParcelable(taxonomy, flags)
        dest?.writeString(description)
        dest?.writeByte((if (heritage) 1 else 0).toByte()) // as boolean value
    }

    companion object {

        /**
         * The name of the 'ID' column.
         */
        const val COLUMN_ID = BaseColumns._ID

        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_HERITAGE = "heritage"

        val DEFAULT_PROJECTION = arrayOf(COLUMN_ID,
                                         COLUMN_NAME,
                                         *Taxonomy.DEFAULT_PROJECTION,
                                         COLUMN_DESCRIPTION,
                                         COLUMN_HERITAGE)
    }
}
