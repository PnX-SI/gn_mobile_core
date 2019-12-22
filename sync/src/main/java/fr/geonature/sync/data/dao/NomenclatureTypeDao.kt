package fr.geonature.sync.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.NomenclatureType
import fr.geonature.commons.data.dao.BaseDao
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder.OrderingTerm.ASC

/**
 * Data access object for [NomenclatureType].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
abstract class NomenclatureTypeDao : BaseDao<NomenclatureType>() {

    /**
     * Internal query builder for [NomenclatureType].
     */
    inner class QB : BaseDao<NomenclatureType>.QB() {

        init {
            selectQueryBuilder.columns(*NomenclatureType.defaultProjection())
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
