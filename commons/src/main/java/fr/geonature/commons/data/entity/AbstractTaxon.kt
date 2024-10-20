package fr.geonature.commons.data.entity

import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Embedded
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder
import fr.geonature.commons.data.helper.sqlEscape
import fr.geonature.commons.data.helper.sqlNormalize
import fr.geonature.compat.os.readParcelableCompat

/**
 * Base taxon.
 *
 * @author S. Grimault
 */
abstract class AbstractTaxon : Parcelable {

    /**
     * The unique ID of the taxon.
     */
    @ColumnInfo(name = COLUMN_ID)
    var id: Long

    /**
     * The default name of the taxon.
     */
    @ColumnInfo(name = COLUMN_NAME)
    var name: String

    @Embedded
    val taxonomy: Taxonomy

    /**
     * The common name of the taxon.
     */
    @ColumnInfo(name = COLUMN_NAME_COMMON)
    var commonName: String?

    /**
     * The description of the taxon.
     */
    @ColumnInfo(name = COLUMN_DESCRIPTION)
    var description: String?

    constructor(
        id: Long,
        name: String,
        taxonomy: Taxonomy,
        commonName: String? = null,
        description: String? = null
    ) {
        this.id = id
        this.name = name
        this.taxonomy = taxonomy
        this.commonName = commonName
        this.description = description
    }

    constructor(source: Parcel) : this(
        source.readLong(),
        source.readString()!!,
        source.readParcelableCompat<Taxonomy>()!!,
        source.readString(),
        source.readString()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractTaxon) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (taxonomy != other.taxonomy) return false
        if (commonName != other.commonName) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + taxonomy.hashCode()
        result = 31 * result + (commonName?.hashCode()
            ?: 0)
        result = 31 * result + (description?.hashCode()
            ?: 0)

        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.also {
            it.writeLong(id)
            it.writeString(name)
            it.writeParcelable(
                taxonomy,
                flags
            )
            it.writeString(commonName)
            it.writeString(description)
        }
    }

    companion object {

        /**
         * The name of the 'ID' column.
         */
        const val COLUMN_ID = BaseColumns._ID

        const val COLUMN_NAME = "name"
        const val COLUMN_NAME_COMMON = "name_common"
        const val COLUMN_DESCRIPTION = "description"

        /**
         * Gets the default projection.
         */
        fun defaultProjection(tableAlias: String): Array<Pair<String, String>> {
            return arrayOf(
                column(
                    COLUMN_ID,
                    tableAlias
                ),
                column(
                    COLUMN_NAME,
                    tableAlias
                ),
                *Taxonomy.defaultProjection(tableAlias),
                column(
                    COLUMN_NAME_COMMON,
                    tableAlias
                ),
                column(
                    COLUMN_DESCRIPTION,
                    tableAlias
                )
            )
        }

        /**
         * Gets alias from given column name.
         */
        fun getColumnAlias(
            columnName: String,
            tableAlias: String
        ): String {
            return column(
                columnName,
                tableAlias
            ).second
        }
    }

    /**
     * Filter query builder.
     */
    open class Filter(internal val tableAlias: String) {
        internal val wheres = mutableListOf<Pair<String, Array<*>?>>()

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
                            COLUMN_NAME_COMMON,
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
                        normalizedQueryString,
                        normalizedQueryString
                    )
                )
            )

            return this
        }

        /**
         * Filter by taxonomy.
         *
         * @return this
         */
        fun byTaxonomy(taxonomy: Taxonomy): Filter {
            if (taxonomy.isAny()) {
                return this
            }

            if (taxonomy.group == Taxonomy.ANY) {
                return byKingdom(taxonomy.kingdom)
            }

            this.wheres.add(
                Pair(
                    "((${
                        Taxonomy.getColumnAlias(
                            Taxonomy.COLUMN_KINGDOM,
                            tableAlias
                        )
                    } = ?) AND (${
                        Taxonomy.getColumnAlias(
                            Taxonomy.COLUMN_GROUP,
                            tableAlias
                        )
                    } = ?))",
                    arrayOf(
                        taxonomy.kingdom.sqlEscape(),
                        taxonomy.group.sqlEscape()
                    )
                )
            )

            return this
        }

        /**
         * Filter by taxonomy kingdom.
         *
         * @return this
         */
        fun byKingdom(kingdom: String): Filter {
            this.wheres.add(
                Pair(
                    "(${
                        Taxonomy.getColumnAlias(
                            Taxonomy.COLUMN_KINGDOM,
                            tableAlias
                        )
                    } = ?)",
                    arrayOf(kingdom.sqlEscape())
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
    open class OrderBy(internal val tableAlias: String) {
        private val orderBy = mutableSetOf<String>()

        /**
         * Adds an ORDER BY statement.
         *
         * @param columnName The selected column name on which to apply order clause.
         * @param orderingTerm The ordering sort order (default: [SQLiteSelectQueryBuilder.OrderingTerm.ASC]).
         *
         * @return this
         */
        fun by(
            columnName: String,
            orderingTerm: SQLiteSelectQueryBuilder.OrderingTerm = SQLiteSelectQueryBuilder.OrderingTerm.ASC
        ): OrderBy {
            this.orderBy.add(
                "${
                    getColumnAlias(
                        columnName,
                        tableAlias
                    )
                } ${orderingTerm.name}"
            )

            return this
        }

        /**
         * Adds an ORDER BY statement on 'name_common' column and on 'name' column from given any
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
                } = '$escapedQueryString' COLLATE NOCASE OR ${
                    getColumnAlias(
                        COLUMN_NAME_COMMON,
                        tableAlias
                    )
                } = '$escapedQueryString' COLLATE NOCASE) THEN 1 WHEN (${
                    getColumnAlias(
                        COLUMN_NAME,
                        tableAlias
                    )
                } LIKE '$escapedQueryString%' COLLATE NOCASE OR ${
                    getColumnAlias(
                        COLUMN_NAME_COMMON,
                        tableAlias
                    )
                } LIKE '$escapedQueryString%' COLLATE NOCASE) THEN 2 WHEN (${
                    getColumnAlias(
                        COLUMN_NAME,
                        tableAlias
                    )
                } LIKE '%$escapedQueryString%' COLLATE NOCASE OR ${
                    getColumnAlias(
                        COLUMN_NAME_COMMON,
                        tableAlias
                    )
                } LIKE '%$escapedQueryString%' COLLATE NOCASE) THEN 3 WHEN (${
                    getColumnAlias(
                        COLUMN_NAME,
                        tableAlias
                    )
                } GLOB '$normalizedQueryString' COLLATE NOCASE OR ${
                    getColumnAlias(
                        COLUMN_NAME_COMMON,
                        tableAlias
                    )
                } GLOB '$normalizedQueryString' COLLATE NOCASE) THEN 4 ELSE 5 END), ${
                    getColumnAlias(
                        COLUMN_NAME,
                        tableAlias
                    )
                } COLLATE NOCASE, ${
                    getColumnAlias(
                        COLUMN_NAME_COMMON,
                        tableAlias
                    )
                } COLLATE NOCASE"
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
