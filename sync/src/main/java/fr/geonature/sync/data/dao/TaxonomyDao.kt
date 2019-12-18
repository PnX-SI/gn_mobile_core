package fr.geonature.sync.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.data.dao.BaseDao
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder.OrderingTerm.ASC

/**
 * Data access object for [Taxonomy].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
abstract class TaxonomyDao : BaseDao<Taxonomy>() {

    /**
     * Internal query builder for [Taxonomy].
     */
    inner class QB : BaseDao<Taxonomy>.QB() {

        init {
            selectQueryBuilder.columns(*Taxonomy.defaultProjection())
                    .orderBy(column(Taxonomy.COLUMN_KINGDOM,
                                    entityTableName).second,
                             ASC)
                    .orderBy(column(Taxonomy.COLUMN_GROUP,
                                    entityTableName).second,
                             ASC)
        }

        fun whereKingdom(kingdom: String): QB {
            selectQueryBuilder.where("${column(Taxonomy.COLUMN_KINGDOM,
                                               entityTableName).second} LIKE ?",
                                     kingdom)

            return this
        }

        fun whereKingdomAndGroup(kingdom: String,
                                 group: String): QB {
            selectQueryBuilder.where("${column(Taxonomy.COLUMN_KINGDOM,
                                               entityTableName).second} LIKE ?",
                                     kingdom)
                    .andWhere("${column(Taxonomy.COLUMN_GROUP,
                                        entityTableName).second} LIKE ?",
                              group)

            return this
        }
    }
}