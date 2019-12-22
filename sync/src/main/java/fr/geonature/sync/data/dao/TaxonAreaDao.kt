package fr.geonature.sync.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.TaxonArea
import fr.geonature.commons.data.dao.BaseDao

/**
 * Data access object for [TaxonArea].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
abstract class TaxonAreaDao : BaseDao<TaxonArea>()
