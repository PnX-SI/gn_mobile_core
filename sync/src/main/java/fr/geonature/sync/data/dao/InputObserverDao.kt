package fr.geonature.sync.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.dao.BaseDao
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder.OrderingTerm.ASC

/**
 * Data access object for [InputObserver].
 */
@Dao
abstract class InputObserverDao : BaseDao<InputObserver>() {

    /**
     * Internal query builder for [InputObserver].
     */
    inner class QB : BaseDao<InputObserver>.QB() {

        init {
            selectQueryBuilder.columns(*InputObserver.defaultProjection())
                .orderBy(
                    column(
                        InputObserver.COLUMN_LASTNAME,
                        entityTableName
                    ).second,
                    ASC,
                    false
                )
                .orderBy(
                    column(
                        InputObserver.COLUMN_FIRSTNAME,
                        entityTableName
                    ).second,
                    ASC,
                    false
                )
        }

        fun whereIdsIn(vararg id: Long): QB {
            selectQueryBuilder.where(
                "${column(
                    InputObserver.COLUMN_ID,
                    entityTableName
                ).second} IN (${id.joinToString(",")})"
            )

            return this
        }

        fun whereId(id: Long?): QB {
            selectQueryBuilder.where(
                "${column(
                    InputObserver.COLUMN_ID,
                    entityTableName
                ).second} = ?",
                id
            )

            return this
        }
    }
}
