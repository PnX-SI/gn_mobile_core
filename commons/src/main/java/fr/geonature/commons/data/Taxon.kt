package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.room.Entity
import fr.geonature.commons.util.get

/**
 * Describes a taxon.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(tableName = Taxon.TABLE_NAME,
        primaryKeys = [AbstractTaxon.COLUMN_ID])
class Taxon : AbstractTaxon {

    constructor(id: Long,
                name: String,
                taxonomy: Taxonomy,
                description: String?,
                heritage: Boolean = false) : super(id,
                                                   name,
                                                   taxonomy,
                                                   description,
                                                   heritage)

    constructor(source: Parcel) : super(source)

    companion object {

        private val TAG = Taxon::class.java.name

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

            return try {
                val taxonomy = Taxonomy.fromCursor(cursor) ?: return null

                Taxon(requireNotNull(cursor.get(COLUMN_ID)),
                      requireNotNull(cursor.get(COLUMN_NAME)),
                      taxonomy,
                      cursor.get(COLUMN_DESCRIPTION),
                      requireNotNull(cursor.get(COLUMN_HERITAGE,
                                                false)))
            }
            catch (iae: IllegalArgumentException) {
                Log.w(TAG,
                      iae.message)

                null
            }
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