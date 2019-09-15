package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.geonature.commons.util.get

/**
 * Describes an input observer.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(tableName = InputObserver.TABLE_NAME)
data class InputObserver(

    /**
     * The unique ID of the input observer.
     */
    @PrimaryKey(autoGenerate = true)
    var id: Long,

    /**
     * The last name of the input observer.
     */
    @ColumnInfo(name = COLUMN_LASTNAME)
    var lastname: String?,

    /**
     * The first name of the input observer.
     */
    @ColumnInfo(name = COLUMN_FIRSTNAME)
    var firstname: String?) : Parcelable {

    private constructor(source: Parcel) : this(source.readLong(),
                                               source.readString(),
                                               source.readString())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeLong(id)
        dest?.writeString(lastname)
        dest?.writeString(firstname)
    }

    companion object {

        private val TAG = InputObserver::class.java.name

        /**
         * The name of the 'observers' table.
         */
        const val TABLE_NAME = "observers"

        /**
         * The name of the 'ID' column.
         */
        const val COLUMN_ID = BaseColumns._ID

        /**
         * The name of the 'lastname' column.
         */
        const val COLUMN_LASTNAME = "lastname"

        /**
         * The name of the 'firstname' column.
         */
        const val COLUMN_FIRSTNAME = "firstname"

        val DEFAULT_PROJECTION = arrayOf(COLUMN_ID,
                                         COLUMN_LASTNAME,
                                         COLUMN_FIRSTNAME)

        /**
         * Create a new [InputObserver] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [InputObserver] instance
         */
        fun fromCursor(cursor: Cursor): InputObserver? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                InputObserver(requireNotNull(cursor.get(COLUMN_ID)),
                              cursor.get(COLUMN_LASTNAME),
                              cursor.get(COLUMN_FIRSTNAME))
            }
            catch (iae: IllegalArgumentException) {
                Log.w(TAG,
                      iae.message)

                null
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<InputObserver> = object : Parcelable.Creator<InputObserver> {

            override fun createFromParcel(source: Parcel): InputObserver {
                return InputObserver(source)
            }

            override fun newArray(size: Int): Array<InputObserver?> {
                return arrayOfNulls(size)
            }
        }
    }
}