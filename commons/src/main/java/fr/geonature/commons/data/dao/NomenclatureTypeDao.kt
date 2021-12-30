package fr.geonature.commons.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder.OrderingTerm.ASC
import fr.geonature.commons.data.model.NomenclatureType

/**
 * Data access object for [NomenclatureType].
 *
 * @author S. Grimault
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
