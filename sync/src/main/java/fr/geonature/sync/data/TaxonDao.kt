package fr.geonature.sync.data

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import fr.geonature.commons.data.Taxon

/**
 * Data access object for [Taxon].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
interface TaxonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg taxa: Taxon)

    /**
     * Select taxa from given query.
     *
     * @param query the query
     *
     * @return A [Cursor] of taxa
     */
    @RawQuery
    fun select(query: SupportSQLiteQuery): Cursor
}