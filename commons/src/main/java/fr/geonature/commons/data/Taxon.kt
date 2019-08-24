package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity

/**
 * Describes a taxon.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(tableName = Taxon.TABLE_NAME)
class Taxon : AbstractTaxon {

    constructor(id: Long,
                name: String,
                description: String?,
                heritage: Boolean = false) : super(id,
                                                   name,
                                                   description,
                                                   heritage)

    constructor(source: Parcel) : super(source)

    companion object {

        /**
         * The name of the 'taxa' table.
         */
        const val TABLE_NAME = "taxa"

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