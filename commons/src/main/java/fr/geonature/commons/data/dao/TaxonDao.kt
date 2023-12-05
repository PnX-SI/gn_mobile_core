package fr.geonature.commons.data.dao

import androidx.room.Dao
import androidx.room.Query
import fr.geonature.commons.data.entity.AbstractTaxon
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.TaxonList
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder

/**
 * Data access object for [Taxon].
 *
 * @author S. Grimault
 */
@Dao
abstract class TaxonDao : BaseDao<Taxon>() {

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
