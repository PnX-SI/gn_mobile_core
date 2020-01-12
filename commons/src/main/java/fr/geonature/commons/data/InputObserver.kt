package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.get

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
    @ColumnInfo(name = COLUMN_ID)
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
    var firstname: String?
) : Parcelable {

    private constructor(source: Parcel) : this(
        source.readLong(),
        source.readString(),
        source.readString()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeLong(id)
            it.writeString(lastname)
            it.writeString(firstname)
        }
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

        /**
         * Gets the default projection.
         */
        fun defaultProjection(tableAlias: String = TABLE_NAME): Array<Pair<String, String>> {
            return arrayOf(
                column(
                    COLUMN_ID,
                    tableAlias
                ),
                column(
                    COLUMN_LASTNAME,
                    tableAlias
                ),
                column(
                    COLUMN_FIRSTNAME,
                    tableAlias
                )
            )
        }

        /**
         * Gets alias from given column name.
         */
        fun getColumnAlias(
            columnName: String,
            tableAlias: String = TABLE_NAME
        ): String {
            return column(
                columnName,
                tableAlias
            ).second
        }

        /**
         * Apply custom filter.
         */
        fun filter(queryString: String?): Pair<String?, Array<String>?> {
            return if (queryString.isNullOrBlank()) Pair(
                null,
                null
            )
            else {
                val filter = "%$queryString%"
                Pair(
                    "${getColumnAlias(COLUMN_LASTNAME)} LIKE ? OR ${getColumnAlias(COLUMN_FIRSTNAME)} LIKE ?",
                    arrayOf(
                        filter,
                        filter
                    )
                )
            }
        }

        /**
         * Create a new [InputObserver] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [InputObserver] instance
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): InputObserver? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                InputObserver(
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_ID,
                                tableAlias
                            )
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_LASTNAME,
                            tableAlias
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_FIRSTNAME,
                            tableAlias
                        )
                    )
                )
            } catch (e: Exception) {
                Log.w(
                    TAG,
                    e.message
                )

                null
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<InputObserver> =
            object : Parcelable.Creator<InputObserver> {

                override fun createFromParcel(source: Parcel): InputObserver {
                    return InputObserver(source)
                }

                override fun newArray(size: Int): Array<InputObserver?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
