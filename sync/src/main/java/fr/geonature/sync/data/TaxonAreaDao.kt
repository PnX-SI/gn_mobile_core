package fr.geonature.sync.data

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import fr.geonature.commons.data.TaxonArea

/**
 * Data access object for [TaxonArea].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
interface TaxonAreaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg taxa: TaxonArea)

    /**
     * Select taxa with area from given query.
     *
     * @param query the query
     *
     * @return A [Cursor] of taxa with areas
     */
    @RawQuery
    fun select(query: SupportSQLiteQuery): Cursor
}