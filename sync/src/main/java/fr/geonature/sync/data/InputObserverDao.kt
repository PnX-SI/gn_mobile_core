package fr.geonature.sync.data

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import fr.geonature.commons.data.InputObserver

/**
 * Data access object for [InputObserver].
 */
@Dao
interface InputObserverDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg inputObservers: InputObserver)

    /**
     * Select observers from given query.
     *
     * @param query the query
     *
     * @return A [Cursor] of observers
     */
    @RawQuery
    fun select(query: SupportSQLiteQuery): Cursor
}