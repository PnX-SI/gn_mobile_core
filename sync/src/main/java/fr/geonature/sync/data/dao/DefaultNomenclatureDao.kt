package fr.geonature.sync.data.dao

import androidx.room.Dao
import fr.geonature.commons.data.DefaultNomenclature
import fr.geonature.commons.data.dao.BaseDao

/**
 * Data access object for [DefaultNomenclature].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
abstract class DefaultNomenclatureDao : BaseDao<DefaultNomenclature>()
