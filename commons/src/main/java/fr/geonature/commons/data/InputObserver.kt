package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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
    @ColumnInfo(index = true,
                name = COLUMN_ID)
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

    private constructor(builder: Builder) : this(builder.id!!,
                                                 builder.lastname,
                                                 builder.firstname)

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

    data class Builder(var id: Long? = null,
                       var lastname: String? = null,
                       var firstname: String? = null) {
        fun id(id: Long) = apply { this.id = id }
        fun lastname(lastname: String?) = apply { this.lastname = lastname }
        fun firstname(firstname: String?) = apply { this.firstname = firstname }

        @Throws(java.lang.IllegalArgumentException::class)
        fun build(): InputObserver {
            if (id == null) throw IllegalArgumentException("InputObserver with null ID is not allowed")

            return InputObserver(this)
        }
    }

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

            return InputObserver(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                                 cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LASTNAME)),
                                 cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRSTNAME)))
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