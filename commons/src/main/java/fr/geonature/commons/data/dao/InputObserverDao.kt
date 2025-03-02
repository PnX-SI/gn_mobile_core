package fr.geonature.commons.data.dao

import androidx.room.Dao
import androidx.room.Query
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder.OrderingTerm.ASC

/**
 * Data access object for [InputObserver].
 *
 * @author S. Grimault
 */
@Dao
abstract class InputObserverDao : BaseDao<InputObserver>() {

    /**
     * Fetches all [InputObserver]s matching the given IDs.
     */
    @Query(
        """SELECT d.*
            FROM ${InputObserver.TABLE_NAME} d
            WHERE d.${InputObserver.COLUMN_ID} IN (:inputObserverId)
        """
    )
    abstract suspend fun findByIds(vararg inputObserverId: Long): List<InputObserver>

    /**
     * Internal query builder for [InputObserver].
     */
    inner class QB : BaseDao<InputObserver>.QB() {

        init {
            selectQueryBuilder
                .columns(*InputObserver.defaultProjection())
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
                "${
                    column(
                        InputObserver.COLUMN_ID,
                        entityTableName
                    ).second
                } IN (${id.joinToString(",")})"
            )

            return this
        }

        fun whereId(id: Long?): QB {
            selectQueryBuilder.where(
                "${
                    column(
                        InputObserver.COLUMN_ID,
                        entityTableName
                    ).second
                } = ?",
                id
            )

            return this
        }
    }
}
