package fr.geonature.sync.data

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import fr.geonature.commons.data.Dataset

/**
 * Data access object for [Dataset].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Dao
interface DatasetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg dataset: Dataset)

    /**
     * Select dataset from given query.
     *
     * @param query the query
     *
     * @return A [Cursor] of dataset
     */
    @RawQuery
    fun select(query: SupportSQLiteQuery): Cursor
}