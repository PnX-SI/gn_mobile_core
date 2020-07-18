package fr.geonature.sync.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.AbstractTaxon
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.TaxonArea
import fr.geonature.commons.data.dao.BaseDao
import fr.geonature.commons.data.helper.EntityHelper.column

/**
 * Data access object for [Taxon].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
abstract class TaxonDao : BaseDao<Taxon>() {

    /**
     * Internal query builder for [Taxon].
     */
    inner class QB : BaseDao<Taxon>.QB() {

        init {
            selectQueryBuilder.columns(*Taxon.defaultProjection())
        }

        fun withArea(id: Long?): QB {
            if (id == null) return this

            selectQueryBuilder.columns(*TaxonArea.defaultProjection())
                .leftJoin(
                    TaxonArea.TABLE_NAME,
                    "${column(
                        TaxonArea.COLUMN_TAXON_ID,
                        TaxonArea.TABLE_NAME
                    ).second} = ${column(
                        AbstractTaxon.COLUMN_ID,
                        entityTableName
                    ).second} AND ${column(
                        TaxonArea.COLUMN_AREA_ID,
                        TaxonArea.TABLE_NAME
                    ).second} = ?",
                    TaxonArea.TABLE_NAME,
                    id
                )

            return this
        }

        fun whereId(id: Long?): QB {
            selectQueryBuilder.where(
                "${column(
                    AbstractTaxon.COLUMN_ID,
                    entityTableName
                ).second} = ?",
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
