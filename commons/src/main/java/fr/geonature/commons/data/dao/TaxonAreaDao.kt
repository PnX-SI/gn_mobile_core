package fr.geonature.commons.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.model.TaxonArea

/**
 * Data access object for [TaxonArea].
 *
 * @author S. Grimault
 */
@Dao
abstract class TaxonAreaDao : BaseDao<TaxonArea>()
