package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import fr.geonature.commons.data.helper.Converters
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder
import fr.geonature.commons.data.helper.get
import fr.geonature.commons.data.helper.sqlEscape
import fr.geonature.commons.data.helper.sqlNormalize
import kotlinx.parcelize.Parcelize
import org.tinylog.Logger
import java.util.Date

/**
 * Describes a dataset.
 *
 * @author S. Grimault
 */
@Entity(
    tableName = Dataset.TABLE_NAME
)
@TypeConverters(Converters::class)
@Parcelize
data class Dataset(

    /**
     * The unique ID of this dataset.
     */
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Long,

    /**
     * The name of the dataset.
     */
    @ColumnInfo(name = COLUMN_NAME) val name: String,

    /**
     * The description of the dataset.
     */
    @ColumnInfo(name = COLUMN_DESCRIPTION) val description: String? = null,

    /**
     * Whether this dataset is active or not.
     */
    @ColumnInfo(name = COLUMN_ACTIVE) val active: Boolean = false,

    /**
     * The creation date of this dataset.
     */
    @ColumnInfo(name = COLUMN_CREATED_AT) val createdAt: Date = Date(),

    /**
     * The updated date of this dataset.
     */
    @ColumnInfo(name = COLUMN_UPDATED_AT) val updatedAt: Date? = null,

    /**
     * The taxa list id of this dataset.
     */
    @ColumnInfo(name = COLUMN_TAXA_LIST_ID) val taxaListId: Long? = null
) : Parcelable {

    companion object {

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
        const val COLUMN_UPDATED_AT = "updated_at"
        const val COLUMN_TAXA_LIST_ID = "taxa_list_id"

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
                    COLUMN_NAME,
                    tableAlias
                ),
                column(
                    COLUMN_DESCRIPTION,
                    tableAlias
                ),
                column(
                    COLUMN_ACTIVE,
                    tableAlias
                ),
                column(
                    COLUMN_CREATED_AT,
                    tableAlias
                ),
                column(
                    COLUMN_UPDATED_AT,
                    tableAlias
                ),
                column(
                    COLUMN_TAXA_LIST_ID,
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
         * Create a new [Dataset] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [Dataset] instance
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): Dataset? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                Dataset(
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_ID,
                                tableAlias
                            )
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_NAME,
                                tableAlias
                            )
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_DESCRIPTION,
                            tableAlias
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_ACTIVE,
                                tableAlias
                            ),
                            false
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_CREATED_AT,
                                tableAlias
                            )
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_UPDATED_AT,
                            tableAlias
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_TAXA_LIST_ID,
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
    }

    /**
     * Filter query builder.
     */
    open class Filter(private val tableAlias: String = TABLE_NAME) {
        private val wheres = mutableListOf<Pair<String, Array<*>?>>()

        /**
         * Filter by name or description.
         *
         * @return this
         */
        fun byNameOrDescription(queryString: String?): Filter {
            if (queryString.isNullOrBlank()) {
                return this
            }

            val normalizedQueryString = queryString.sqlNormalize()

            this.wheres.add(
                Pair(
                    "(${
                        getColumnAlias(
                            COLUMN_NAME,
                            tableAlias
                        )
                    } GLOB ? OR ${
                        getColumnAlias(
                            COLUMN_DESCRIPTION,
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
         * Adds an ORDER BY statement on 'name' column  from given any query string.
         * The default sort order is [SQLiteSelectQueryBuilder.OrderingTerm.ASC].
         *
         * @param queryString The query string.
         *
         * @return this
         */
        fun byNameOrDescription(queryString: String? = null): OrderBy {
            if (queryString.isNullOrBlank()) {
                this.orderBy.add(
                    "${
                        getColumnAlias(
                            COLUMN_NAME,
                            tableAlias
                        )
                    } ${SQLiteSelectQueryBuilder.OrderingTerm.ASC.name}"
                )

                return this
            }

            val escapedQueryString = queryString.sqlEscape()
            val normalizedQueryString = queryString.sqlNormalize()

            this.orderBy.add(
                "(CASE WHEN (${
                    getColumnAlias(
                        COLUMN_NAME,
                        tableAlias
                    )
                } = '$escapedQueryString' OR ${
                    getColumnAlias(
                        COLUMN_DESCRIPTION,
                        tableAlias
                    )
                } = '$escapedQueryString') THEN 1 WHEN (${
                    getColumnAlias(
                        COLUMN_NAME,
                        tableAlias
                    )
                } LIKE '%$escapedQueryString%' OR ${
                    getColumnAlias(
                        COLUMN_DESCRIPTION,
                        tableAlias
                    )
                } LIKE '%$escapedQueryString%') THEN 2 WHEN (${
                    getColumnAlias(
                        COLUMN_NAME,
                        tableAlias
                    )
                } GLOB '$normalizedQueryString' OR ${
                    getColumnAlias(
                        COLUMN_DESCRIPTION,
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
