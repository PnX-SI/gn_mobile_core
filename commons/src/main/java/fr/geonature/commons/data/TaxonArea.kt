package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.TypeConverters
import fr.geonature.commons.data.Converters.fromTimestamp
import fr.geonature.commons.util.get
import java.util.Date

/**
 * Describes a taxon data within an area.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(tableName = TaxonArea.TABLE_NAME,
        primaryKeys = [TaxonArea.COLUMN_TAXON_ID, TaxonArea.COLUMN_AREA_ID],
        foreignKeys = [ForeignKey(entity = Taxon::class,
                                  parentColumns = [AbstractTaxon.COLUMN_ID],
                                  childColumns = [TaxonArea.COLUMN_TAXON_ID],
                                  onDelete = ForeignKey.CASCADE)])
@TypeConverters(Converters::class)
data class TaxonArea(

    /**
     * The foreign key taxon ID of taxon.
     */
    @ColumnInfo(name = COLUMN_TAXON_ID)
    var taxonId: Long,

    @ColumnInfo(name = COLUMN_AREA_ID)
    var areaId: Long,

    @ColumnInfo(name = COLUMN_COLOR)
    var color: String?,

    @ColumnInfo(name = COLUMN_NUMBER_OF_OBSERVERS)
    var numberOfObservers: Int,

    @ColumnInfo(name = COLUMN_LAST_UPDATED_AT)
    var lastUpdatedAt: Date?) : Parcelable {

    private constructor(source: Parcel) : this(source.readLong(),
                                               source.readLong(),
                                               source.readString(),
                                               source.readInt(),
                                               source.readSerializable() as Date)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeLong(taxonId)
        dest?.writeLong(areaId)
        dest?.writeString(color)
        dest?.writeInt(numberOfObservers)
        dest?.writeSerializable(lastUpdatedAt)
    }

    companion object {

        private val TAG = TaxonArea::class.java.name

        /**
         * The name of the 'taxa_area' table.
         */
        const val TABLE_NAME = "taxa_area"

        const val COLUMN_TAXON_ID = "taxon_id"
        const val COLUMN_AREA_ID = "area_id"
        const val COLUMN_COLOR = "color"
        const val COLUMN_NUMBER_OF_OBSERVERS = "nb_observers"
        const val COLUMN_LAST_UPDATED_AT = "last_updated_at"

        val DEFAULT_PROJECTION = arrayOf(COLUMN_TAXON_ID,
                                         COLUMN_AREA_ID,
                                         COLUMN_COLOR,
                                         COLUMN_NUMBER_OF_OBSERVERS,
                                         COLUMN_LAST_UPDATED_AT)

        /**
         * Create a new [TaxonArea] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [TaxonArea] instance
         */
        fun fromCursor(cursor: Cursor): TaxonArea? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                TaxonArea(requireNotNull(cursor.get(COLUMN_TAXON_ID)),
                          requireNotNull(cursor.get(COLUMN_AREA_ID)),
                          cursor.get(COLUMN_COLOR,
                                     "#00000000"),
                          requireNotNull(cursor.get(COLUMN_NUMBER_OF_OBSERVERS,
                                                    0)),
                          cursor.get(COLUMN_LAST_UPDATED_AT,
                                     0L).run { if (this == 0L) null else fromTimestamp(this) })
            }
            catch (iae: IllegalArgumentException) {
                Log.w(TAG,
                      iae.message)

                null
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<TaxonArea> = object : Parcelable.Creator<TaxonArea> {

            override fun createFromParcel(source: Parcel): TaxonArea {
                return TaxonArea(source)
            }

            override fun newArray(size: Int): Array<TaxonArea?> {
                return arrayOfNulls(size)
            }
        }
    }
}