package fr.geonature.commons.data

import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Embedded
import fr.geonature.commons.data.helper.EntityHelper.column

/**
 * Base taxon.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
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

    /**
     * The rank description of the taxon.
     */
    @ColumnInfo(name = COLUMN_RANK)
    var rank: String?

    constructor(
        id: Long,
        name: String,
        taxonomy: Taxonomy,
        commonName: String? = null,
        description: String? = null,
        rank: String? = null
    ) {
        this.id = id
        this.name = name
        this.taxonomy = taxonomy
        this.commonName = commonName
        this.description = description
        this.rank = rank
    }

    constructor(source: Parcel) : this(
        source.readLong(),
        source.readString()!!,
        source.readParcelable(Taxonomy::class.java.classLoader)!!,
        source.readString(),
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
        if (rank != other.rank) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + taxonomy.hashCode()
        result = 31 * result + (commonName?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (rank?.hashCode() ?: 0)

        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeLong(id)
            it.writeString(name)
            it.writeParcelable(
                taxonomy,
                flags
            )
            it.writeString(commonName)
            it.writeString(description)
            it.writeString(rank)
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
        const val COLUMN_RANK = "rank"

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
                ),
                column(
                    COLUMN_RANK,
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
        fun byNameOrDescriptionOrRank(queryString: String?): Filter {
            if (queryString.isNullOrBlank()) {
                return this
            }

            this.wheres.add(
                Pair(
                    "(${getColumnAlias(
                        COLUMN_NAME,
                        tableAlias
                    )} LIKE ? OR ${getColumnAlias(
                        COLUMN_NAME_COMMON,
                        tableAlias
                    )} LIKE ? OR ${getColumnAlias(
                        COLUMN_DESCRIPTION,
                        tableAlias
                    )} LIKE ? OR ${getColumnAlias(
                        COLUMN_RANK,
                        tableAlias
                    )} LIKE ?)",
                    arrayOf(
                        "%$queryString%",
                        "%$queryString%",
                        "%$queryString%",
                        "%$queryString%"
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
                    "((${Taxonomy.getColumnAlias(
                        Taxonomy.COLUMN_KINGDOM,
                        tableAlias
                    )} = ?) AND (${Taxonomy.getColumnAlias(
                        Taxonomy.COLUMN_GROUP,
                        tableAlias
                    )} = ?))",
                    arrayOf(
                        taxonomy.kingdom,
                        taxonomy.group
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
                    "(${Taxonomy.getColumnAlias(
                        Taxonomy.COLUMN_KINGDOM,
                        tableAlias
                    )} = ?)",
                    arrayOf(kingdom)
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
                pair.second?.toList()
                    ?.also { bindArgs.addAll(it) }
                pair.first
            }

            return Pair(
                whereClauses,
                bindArgs.toTypedArray()
            )
        }
    }
}
