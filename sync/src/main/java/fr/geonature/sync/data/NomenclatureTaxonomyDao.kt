package fr.geonature.sync.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import fr.geonature.commons.data.NomenclatureTaxonomy

/**
 * Data access object for [NomenclatureTaxonomy].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
interface NomenclatureTaxonomyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg nomenclatureType: NomenclatureTaxonomy)
}