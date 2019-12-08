package fr.geonature.sync.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.NomenclatureTaxonomy
import fr.geonature.commons.data.dao.BaseDao

/**
 * Data access object for [NomenclatureTaxonomy].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
abstract class NomenclatureTaxonomyDao : BaseDao<NomenclatureTaxonomy>()