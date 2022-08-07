package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.EntityHelper.normalize
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder
import fr.geonature.commons.data.helper.get
import org.tinylog.Logger

/**
 * Describes an input observer.
 *
 * @author S. Grimault
 */
@Entity(tableName = InputObserver.TABLE_NAME)
data class InputObserver(

    /**
     * The unique ID of the input observer.
     */
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Long,

    /**
     * The last name of the input observer.
     */
    @ColumnInfo(name = COLUMN_LASTNAME) var lastname: String?,

    /**
     * The first name of the input observer.
     */
    @ColumnInfo(name = COLUMN_FIRSTNAME) var firstname: String?
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
                e.message?.run {
                    Logger.warn { this }
                }

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

    /**
     * Filter query builder.
     */
    open class Filter(private val tableAlias: String = TABLE_NAME) {
        private val wheres = mutableListOf<Pair<String, Array<*>?>>()

        /**
         * Filter by name.
         *
         * @return this
         */
        fun byName(queryString: String?): Filter {
            if (queryString.isNullOrBlank()) {
                return this
            }

            val normalizedQueryString = normalize(queryString)

            this.wheres.add(
                Pair(
                    "(${
                        getColumnAlias(
                            COLUMN_LASTNAME,
                            tableAlias
                        )
                    } GLOB ? OR ${
                        getColumnAlias(
                            COLUMN_FIRSTNAME,
                            tableAlias
                        )
                    } GLOB ?)",
                    arrayOf(
                        normalizedQueryString,
                        normalizedQueryString
                    )
                )
            )

            return this
        }

        /**
         * Builds the WHERE clause as selection for this filter.
         */
        fun build(): Pair<String, Array<Any?>> {
            val bindArgs = mutableListOf<Any?>()

            val whereClauses = this.wheres.joinToString(" AND ") { pair ->
                pair.second
                    ?.toList()
                    ?.also { bindArgs.addAll(it) }
                pair.first
            }

            return Pair(
                whereClauses,
                bindArgs.toTypedArray()
            )
        }
    }

    /**
     * Order by query builder.
     */
    open class OrderBy(private val tableAlias: String = TABLE_NAME) {
        private val orderBy = mutableSetOf<String>()

        /**
         * Adds an ORDER BY statement on 'lastname' column and on 'firstname' column from given any
         * query string. The default sort order is [SQLiteSelectQueryBuilder.OrderingTerm.ASC].
         *
         * @param queryString The query string.
         *
         * @return this
         */
        fun byName(queryString: String? = null): OrderBy {
            if (queryString.isNullOrBlank()) {
                this.orderBy.add(
                    "${
                        getColumnAlias(
                            COLUMN_LASTNAME,
                            tableAlias
                        )
                    } ${SQLiteSelectQueryBuilder.OrderingTerm.ASC.name}"
                )

                return this
            }

            val normalizedQueryString = normalize(queryString)

            this.orderBy.add(
                "(CASE WHEN (${
                    getColumnAlias(
                        COLUMN_LASTNAME,
                        tableAlias
                    )
                } = '$queryString' OR ${
                    getColumnAlias(
                        COLUMN_FIRSTNAME,
                        tableAlias
                    )
                } = '$queryString') THEN 1 WHEN (${
                    getColumnAlias(
                        COLUMN_LASTNAME,
                        tableAlias
                    )
                } LIKE '%$queryString%' OR ${
                    getColumnAlias(
                        COLUMN_FIRSTNAME,
                        tableAlias
                    )
                } LIKE '%$queryString%') THEN 2 WHEN (${
                    getColumnAlias(
                        COLUMN_LASTNAME,
                        tableAlias
                    )
                } GLOB '$normalizedQueryString' OR ${
                    getColumnAlias(
                        COLUMN_FIRSTNAME,
                        tableAlias
                    )
                } GLOB '$normalizedQueryString') THEN 3 ELSE 4 END)"
            )

            return this
        }

        /**
         * Builds the ORDER BY clause.
         */
        fun build(): String? {
            if (this.orderBy.isEmpty()) {
                return null
            }

            return this.orderBy.joinToString(", ")
        }
    }
}
