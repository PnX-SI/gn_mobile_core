package fr.geonature.sync.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.Dataset
import fr.geonature.commons.data.dao.BaseDao
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder.OrderingTerm.ASC

/**
 * Data access object for [Dataset].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
abstract class DatasetDao : BaseDao<Dataset>() {

    /**
     * Internal query builder for [Dataset].
     */
    inner class QB : BaseDao<Dataset>.QB() {

        init {
            selectQueryBuilder.columns(*Dataset.defaultProjection())
                .orderBy(
                    column(
                        Dataset.COLUMN_NAME,
                        entityTableName
                    ).second,
                    ASC,
                    false
                )
        }

        fun whereModule(module: String?): QB {
            if (module.isNullOrBlank()) {
                return this
            }

            selectQueryBuilder.andWhere(
                "${column(
                    Dataset.COLUMN_MODULE,
                    entityTableName
                ).second} = ?",
                module
            )

            return this
        }

        fun whereActive(): QB {
            selectQueryBuilder.andWhere(
                "${column(
                    Dataset.COLUMN_ACTIVE,
                    entityTableName
                ).second} = 1"
            )

            return this
        }

        fun whereId(id: Long?): QB {
            selectQueryBuilder.andWhere(
                "${column(
                    Dataset.COLUMN_ID,
                    entityTableName
                ).second} = ?",
                id
            )

            return this
        }
    }
}
