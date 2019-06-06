package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Describes a taxon.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(tableName = Taxon.TABLE_NAME)
data class Taxon(

    /**
     * The unique ID of the taxon.
     */
    @PrimaryKey(autoGenerate = true) @ColumnInfo(index = true,
                                                 name = COLUMN_ID) var id: Long,

    /**
     * The default name of the taxon.
     */
    @ColumnInfo(name = COLUMN_NAME) var name: String?,

    /**
     * The info of the taxon.
     */
    @ColumnInfo(name = COLUMN_DESCRIPTION) var description: String?,

    /**
     * Whether the taxon is part of the heritage.
     */
    @ColumnInfo(name = COLUMN_HERITAGE) var heritage: Boolean) : Parcelable {

    private constructor(builder: Builder) : this(builder.id!!,
                                                 builder.name,
                                                 builder.description,
                                                 builder.heritage)

    private constructor(source: Parcel) : this(source.readLong(),
                                               source.readString(),
                                               source.readString(),
                                               source.readByte() == 1.toByte())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeLong(id)
        dest?.writeString(name)
        dest?.writeString(description)
        dest?.writeByte((if (heritage) 1 else 0).toByte()) // as boolean value
    }

    data class Builder(var id: Long? = null,
                       var name: String? = null,
                       var description: String? = null,
                       var heritage: Boolean = false) {
        fun id(id: Long) = apply { this.id = id }
        fun name(name: String?) = apply { this.name = name }
        fun description(description: String?) = apply { this.description = description }
        fun heritage(heritage: Boolean) = apply { this.heritage = heritage }

        @Throws(java.lang.IllegalArgumentException::class)
        fun build(): Taxon {
            if (id == null) throw IllegalArgumentException("Taxon with null ID is not allowed")

            return Taxon(this)
        }
    }

    companion object {

        /**
         * The name of the 'taxa' table.
         */
        const val TABLE_NAME = "taxa"

        /**
         * The name of the 'ID' column.
         */
        const val COLUMN_ID = BaseColumns._ID

        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_HERITAGE = "heritage"

        /**
         * Create a new [Taxon] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [Taxon] instance
         */
        fun fromCursor(cursor: Cursor): Taxon? {
            if (cursor.isClosed) {
                return null
            }

            return Taxon(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                         cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                         cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                         cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HERITAGE))?.toBoolean()
                             ?: false)
        }

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<Taxon> = object : Parcelable.Creator<Taxon> {

            override fun createFromParcel(source: Parcel): Taxon {
                return Taxon(source)
            }

            override fun newArray(size: Int): Array<Taxon?> {
                return arrayOfNulls(size)
            }
        }
    }
}