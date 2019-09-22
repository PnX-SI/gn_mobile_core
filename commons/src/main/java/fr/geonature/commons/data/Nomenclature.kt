package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import fr.geonature.commons.util.get

/**
 * Describes a nomenclature item.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(tableName = Nomenclature.TABLE_NAME)
open class Nomenclature : Parcelable {

    /**
     * The unique ID of this nomenclature.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    var id: Long

    @ColumnInfo(name = COLUMN_CODE)
    var code: String

    @ColumnInfo(name = COLUMN_HIERARCHY)
    var hierarchy: String

    @ColumnInfo(name = COLUMN_DEFAULT_LABEL)
    var defaultLabel: String

    @ForeignKey(entity = NomenclatureType::class,
                parentColumns = [NomenclatureType.COLUMN_ID],
                childColumns = [COLUMN_TYPE_ID],
                onDelete = CASCADE)
    @ColumnInfo(name = COLUMN_TYPE_ID)
    var typeId: Long

    constructor(id: Long,
                code: String,
                hierarchy: String,
                defaultLabel: String,
                typeId: Long) {
        this.id = id
        this.code = code
        this.hierarchy = hierarchy
        this.defaultLabel = defaultLabel
        this.typeId = typeId
    }

    internal constructor(source: Parcel) : this(source.readLong(),
                                                source.readString()!!,
                                                source.readString()!!,
                                                source.readString()!!,
                                                source.readLong())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Nomenclature) return false

        if (id != other.id) return false
        if (code != other.code) return false
        if (hierarchy != other.hierarchy) return false
        if (defaultLabel != other.defaultLabel) return false
        if (typeId != other.typeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + hierarchy.hashCode()
        result = 31 * result + defaultLabel.hashCode()
        result = 31 * result + typeId.hashCode()

        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeLong(id)
        dest?.writeString(code)
        dest?.writeString(hierarchy)
        dest?.writeString(defaultLabel)
        dest?.writeLong(typeId)
    }

    companion object {

        private val TAG = Nomenclature::class.java.name

        /**
         * The name of the 'nomenclatures' table.
         */
        const val TABLE_NAME = "nomenclatures"

        /**
         * The name of the 'ID' column.
         */
        const val COLUMN_ID = BaseColumns._ID

        const val COLUMN_CODE = "code"
        const val COLUMN_HIERARCHY = "hierarchy"
        const val COLUMN_DEFAULT_LABEL = "default_label"
        const val COLUMN_TYPE_ID = "type_id"

        val DEFAULT_PROJECTION = arrayOf(COLUMN_ID,
                                         COLUMN_CODE,
                                         COLUMN_HIERARCHY,
                                         COLUMN_DEFAULT_LABEL,
                                         COLUMN_TYPE_ID)

        /**
         * Create a new [Nomenclature] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [Nomenclature] instance
         */
        fun fromCursor(cursor: Cursor): Nomenclature? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                Nomenclature(requireNotNull(cursor.get(COLUMN_ID)),
                             requireNotNull(cursor.get(COLUMN_CODE)),
                             requireNotNull(cursor.get(COLUMN_HIERARCHY)),
                             requireNotNull(cursor.get(COLUMN_DEFAULT_LABEL)),
                             requireNotNull(cursor.get(COLUMN_TYPE_ID)))
            }
            catch (iae: IllegalArgumentException) {
                Log.w(TAG,
                      iae.message)

                null
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Nomenclature> = object : Parcelable.Creator<Nomenclature> {

            override fun createFromParcel(source: Parcel): Nomenclature {
                return Nomenclature(source)
            }

            override fun newArray(size: Int): Array<Nomenclature?> {
                return arrayOfNulls(size)
            }
        }
    }
}