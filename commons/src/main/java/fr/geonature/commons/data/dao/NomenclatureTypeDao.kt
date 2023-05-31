package fr.geonature.commons.data.dao

import androidx.room.Dao
import androidx.room.Query
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder.OrderingTerm.ASC

/**
 * Data access object for [NomenclatureType].
 *
 * @author S. Grimault
 */
@Dao
abstract class NomenclatureTypeDao : BaseDao<NomenclatureType>() {

    /**
     * Fetches all [NomenclatureType].
     *
     * @return a list of [NomenclatureType]
     */
    @Query(
        """SELECT * FROM ${NomenclatureType.TABLE_NAME}
            ORDER BY ${NomenclatureType.COLUMN_MNEMONIC} ASC"""
    )
    abstract suspend fun findAllOrderByMnemonic(): List<NomenclatureType>

    /**
     * Internal query builder for [NomenclatureType].
     */
    inner class QB : BaseDao<NomenclatureType>.QB() {

        init {
            selectQueryBuilder
                .columns(*NomenclatureType.defaultProjection())
                .orderBy(
                    column(
                        NomenclatureType.COLUMN_MNEMONIC,
                        entityTableName
                    ).second,
                    ASC
                )
        }
    }
}
