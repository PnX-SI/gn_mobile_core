package fr.geonature.datasync.sync

import androidx.lifecycle.LiveData
import java.util.Date

/**
 * Manage synchronization statuses.
 *
 * @author S. Grimault
 */
interface IDataSyncManager {

    /**
     * The last data synchronization date (or `null` if never done).
     */
    val lastSynchronizedDate: LiveData<Pair<SyncState, Date?>>

    fun updateLastSynchronizedDate(complete: Boolean = true)

    fun getLastSynchronizedDate(): Pair<SyncState, Date?>

    enum class SyncState {
        FULL,
        ESSENTIAL
    }
}