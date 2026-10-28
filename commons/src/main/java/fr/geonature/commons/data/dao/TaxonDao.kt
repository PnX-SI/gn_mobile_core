package fr.geonature.commons.data.dao

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Dao
import androidx.room.Query
import androidx.sqlite.db.SimpleSQLiteQuery
import fr.geonature.commons.data.entity.AbstractTaxon
import fr.geonature.commons.data.entity.AbstractTaxon.Companion.COLUMN_DESCRIPTION
import fr.geonature.commons.data.entity.AbstractTaxon.Companion.COLUMN_NAME
import fr.geonature.commons.data.entity.AbstractTaxon.Companion.COLUMN_NAME_COMMON
import fr.geonature.commons.data.entity.AbstractTaxon.Companion.getColumnAlias
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.TaxonFts
import fr.geonature.commons.data.entity.TaxonList
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder
import fr.geonature.commons.data.helper.sqlNormalize

/**
 * Data access object for [Taxon].
 *
 * @author S. Grimault
 */
@Dao
abstract class TaxonDao : BaseDao<Taxon>() {

    override fun deleteAll() {
        super.deleteAll()

        if (Build.VERSION.SDK_INT >= 30) {
            query(SimpleSQLiteQuery("DELETE FROM ${TaxonFts.TABLE_NAME}")).moveToFirst()
        }
    }

    @Query(
        """SELECT t.${AbstractTaxon.COLUMN_ID}
            FROM ${Taxon.TABLE_NAME} t
        """
    )
    abstract suspend fun findAllIds(): List<Long>

    @Query(
        """SELECT t.*
            FROM ${Taxon.TABLE_NAME} t
            WHERE t.${AbstractTaxon.COLUMN_ID} = :taxonId
        """
    )
    abstract suspend fun findById(taxonId: Long): Taxon?

    @Query(
        """SELECT t.*
            FROM ${Taxon.TABLE_NAME} t
            WHERE t.${AbstractTaxon.COLUMN_ID} IN (:taxonIds)
        """
    )
    abstract suspend fun findByIds(vararg taxonIds: Long): List<Taxon>

    @Query(
        """SELECT
            t.*,
            ta.*
            FROM ${Taxon.TABLE_NAME} t
            LEFT JOIN ${TaxonArea.TABLE_NAME} ta ON ta.${TaxonArea.COLUMN_TAXON_ID} = t.${AbstractTaxon.COLUMN_ID} AND ta.${TaxonArea.COLUMN_AREA_ID} = :areaId
            WHERE t.${AbstractTaxon.COLUMN_ID} = :taxonId
        """
    )
    abstract suspend fun findByIdMatchingArea(
        taxonId: Long,
        areaId: Long
    ): Map<Taxon, TaxonArea?>

    @Query("DELETE FROM ${Taxon.TABLE_NAME} WHERE ${AbstractTaxon.COLUMN_ID} = :id")
    abstract fun deleteById(id: Long)

    /**
     * Internal query builder for [Taxon].
     */
    inner class QB : BaseDao<Taxon>.QB() {

        init {
            selectQueryBuilder.columns(*Taxon.defaultProjection())
        }

        /**
         * Filter by taxa list ID.
         */
        fun withListId(listId: Long?): QB {
            if (listId == null) return this

            selectQueryBuilder
                .columns(*TaxonList.defaultProjection())
                .join(
                    SQLiteSelectQueryBuilder.JoinOperator.DEFAULT,
                    TaxonList.TABLE_NAME,
                    "${
                        column(
                            TaxonList.COLUMN_TAXON_ID,
                            TaxonList.TABLE_NAME
                        ).second
                    } = ${
                        column(
                            AbstractTaxon.COLUMN_ID,
                            entityTableName
                        ).second
                    } AND ${
                        column(
                            TaxonList.COLUMN_TAXA_LIST_ID,
                            TaxonList.TABLE_NAME
                        ).second
                    } = ?",
                    TaxonList.TABLE_NAME,
                    listId
                )

            return this
        }

        /**
         * Adds taxa area matching area ID.
         */
        fun withArea(id: Long?): QB {
            if (id == null) return this

            selectQueryBuilder
                .columns(*TaxonArea.defaultProjection())
                .leftJoin(
                    TaxonArea.TABLE_NAME,
                    "${
                        column(
                            TaxonArea.COLUMN_TAXON_ID,
                            TaxonArea.TABLE_NAME
                        ).second
                    } = ${
                        column(
                            AbstractTaxon.COLUMN_ID,
                            entityTableName
                        ).second
                    } AND ${
                        column(
                            TaxonArea.COLUMN_AREA_ID,
                            TaxonArea.TABLE_NAME
                        ).second
                    } = ?",
                    TaxonArea.TABLE_NAME,
                    id
                )

            return this
        }

        /**
         * Filter by name or description using where clause.
         *
         * @return this
         */
        fun whereNameOrDescription(queryString: String?): QB {
            if (queryString.isNullOrBlank()) {
                return this
            }

            val normalizedQueryString = queryString.sqlNormalize()

            selectQueryBuilder.andWhere(
                "(${
                    getColumnAlias(
                        COLUMN_NAME,
                        Taxon.TABLE_NAME
                    )
                } GLOB ? OR ${
                    getColumnAlias(
                        COLUMN_NAME_COMMON,
                        Taxon.TABLE_NAME
                    )
                } GLOB ? OR ${
                    getColumnAlias(
                        COLUMN_DESCRIPTION,
                        Taxon.TABLE_NAME
                    )
                } GLOB ?)",
                *arrayOf(
                    normalizedQueryString,
                    normalizedQueryString,
                    normalizedQueryString
                )
            )

            return this
        }

        /**
         * Full-text search by name or description.
         *
         * @return this
         */
        @RequiresApi(api = 30)
        fun whereNameOrDescriptionMatch(queryString: String?): QB {
            if (queryString.isNullOrBlank()) {
                return this
            }

            selectQueryBuilder
                .join(
                    joinOperator = SQLiteSelectQueryBuilder.JoinOperator.DEFAULT,
                    tableName = TaxonFts.TABLE_NAME,
                    joinConstraint = "${
                        column(
                            AbstractTaxon.COLUMN_ID,
                            TaxonFts.TABLE_NAME
                        ).first
                    } = ${
                        column(
                            AbstractTaxon.COLUMN_ID,
                            entityTableName
                        ).second
                    }",
                    alias = TaxonFts.TABLE_NAME
                )
                .andWhere(
                    "${TaxonFts.TABLE_NAME} MATCH ?",
                    queryString.let {
                        if (arrayOf(
                                "*",
                                "^",
                                "AND",
                                "OR",
                                "NOT"
                            ).any { p -> it.contains(p) }
                        ) {
                            it
                        } else {
                            it
                                .split("\\s".toRegex())
                                .joinToString(" ") { t -> "*$t*" }
                        }
                    })

            return this
        }

        /**
         * Filter by taxon ID.
         */
        fun whereId(id: Long?): QB {
            selectQueryBuilder.where(
                "${
                    column(
                        AbstractTaxon.COLUMN_ID,
                        entityTableName
                    ).second
                } = ?",
                id
            )

            return this
        }

        fun orderBy(orderByClause: String): QB {
            selectQueryBuilder.orderBy(orderByClause)

            return this
        }
    }
}
