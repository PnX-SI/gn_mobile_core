package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import fr.geonature.commons.util.get
import java.util.Date

/**
 * Describes a dataset.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(tableName = Dataset.TABLE_NAME)
@TypeConverters(Converters::class)
data class Dataset(
    /**
     * The unique ID of the input observer.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    var id: Long,

    /**
     * The name of the dataset.
     */
    @ColumnInfo(name = COLUMN_NAME)
    var name: String,

    /**
     * The description of the dataset.
     */
    @ColumnInfo(name = COLUMN_DESCRIPTION)
    var description: String?,

    /**
     * Whether this dataset is active or not.
     */
    @ColumnInfo(name = COLUMN_ACTIVE)
    var active: Boolean = false,

    /**
     * The creation date of this dataset.
     */
    @ColumnInfo(name = COLUMN_CREATED_AT)
    var createdAt: Date?) : Parcelable {

    private constructor(source: Parcel) : this(source.readLong(),
                                               source.readString()!!,
                                               source.readString(),
                                               source.readByte() == 1.toByte(),
                                               source.readSerializable() as Date)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.also {
            it.writeLong(id)
            it.writeString(name)
            it.writeString(description)
            it.writeByte((if (active) 1 else 0).toByte()) // as boolean value
            it.writeSerializable(createdAt)
        }
    }

    companion object {

        private val TAG = Dataset::class.java.name

        /**
         * The name of the 'observers' table.
         */
        const val TABLE_NAME = "dataset"

        /**
         * The name of the 'ID' column.
         */
        const val COLUMN_ID = BaseColumns._ID

        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_ACTIVE = "active"
        const val COLUMN_CREATED_AT = "created_at"

        val DEFAULT_PROJECTION = arrayOf(COLUMN_ID,
                                         COLUMN_NAME,
                                         COLUMN_DESCRIPTION,
                                         COLUMN_ACTIVE,
                                         COLUMN_CREATED_AT)

        /**
         * Create a new [Dataset] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [Dataset] instance
         */
        fun fromCursor(cursor: Cursor): Dataset? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                Dataset(requireNotNull(cursor.get(COLUMN_ID)),
                        requireNotNull(cursor.get(COLUMN_NAME)),
                        cursor.get(COLUMN_DESCRIPTION),
                        requireNotNull(cursor.get(COLUMN_ACTIVE,
                                                  false)),
                        cursor.get(COLUMN_CREATED_AT))
            }
            catch (iae: IllegalArgumentException) {
                Log.w(TAG,
                      iae.message)

                null
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Dataset> = object : Parcelable.Creator<Dataset> {

            override fun createFromParcel(source: Parcel): Dataset {
                return Dataset(source)
            }

            override fun newArray(size: Int): Array<Dataset?> {
                return arrayOfNulls(size)
            }
        }
    }
}