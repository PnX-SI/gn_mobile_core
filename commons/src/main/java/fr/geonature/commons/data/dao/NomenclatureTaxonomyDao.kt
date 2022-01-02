package fr.geonature.commons.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.entity.NomenclatureTaxonomy

/**
 * Data access object for [NomenclatureTaxonomy].
 *
 * @author S. Grimault
 */
@Dao
abstract class NomenclatureTaxonomyDao : BaseDao<NomenclatureTaxonomy>()
