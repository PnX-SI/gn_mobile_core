package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Describes an input observer.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(tableName = InputObserver.TABLE_NAME)
@Parcelize
data class InputObserver(

    /**
     * The unique ID of the input observer.
     */
    @PrimaryKey(autoGenerate = true) @ColumnInfo(index = true,
                                                 name = COLUMN_ID) var id: Long,
    /**
     * The last name of the input observer.
     */
    @ColumnInfo(name = COLUMN_LASTNAME) var lastname: String?,

    /**
     * The first name of the input observer.
     */
    @ColumnInfo(name = COLUMN_FIRSTNAME) var firstname: String?) : Parcelable {

    companion object {

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

            return InputObserver(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                                 cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LASTNAME)),
                                 cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRSTNAME)))
        }
    }
}