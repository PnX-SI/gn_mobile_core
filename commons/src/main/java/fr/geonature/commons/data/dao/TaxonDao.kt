package fr.geonature.commons.data.dao

import androidx.room.Dao
import androidx.room.Query
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.model.AbstractTaxon
import fr.geonature.commons.data.model.Taxon
import fr.geonature.commons.data.model.TaxonArea

/**
 * Data access object for [Taxon].
 *
 * @author S. Grimault
 */
@Dao
abstract class TaxonDao : BaseDao<Taxon>() {

    @Query("DELETE FROM ${Taxon.TABLE_NAME} WHERE ${AbstractTaxon.COLUMN_ID} = :id")
    abstract fun deleteById(id: Long)

    /**
     * Internal query builder for [Taxon].
     */
    inner class QB : BaseDao<Taxon>.QB() {

        init {
            selectQueryBuilder.columns(*Taxon.defaultProjection())
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
