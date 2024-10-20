package fr.geonature.commons.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.entity.TaxonList

/**
 * Data access object for [TaxonList].
 *
 * @author S. Grimault
 */
@Dao
abstract class TaxonListDao : BaseDao<TaxonList>()