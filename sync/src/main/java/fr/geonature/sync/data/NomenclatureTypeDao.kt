package fr.geonature.sync.data

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import fr.geonature.commons.data.NomenclatureType

/**
 * Data access object for [NomenclatureType].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
interface NomenclatureTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg nomenclatureType: NomenclatureType)

    /**
     * Select nomenclature types from given query.
     *
     * @param query the query
     *
     * @return A [Cursor] of taxa
     */
    @RawQuery
    fun select(query: SupportSQLiteQuery): Cursor
}