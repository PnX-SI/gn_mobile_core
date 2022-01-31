package fr.geonature.datasync.sync

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.geonature.commons.data.dao.AppSyncDao
import java.util.Date

/**
 * Manage synchronization statuses.
 *
 * @author S. Grimault
 */
class DataSyncManagerImpl constructor(private val appSyncDao: AppSyncDao) : IDataSyncManager {

    private val _lastSynchronizedDate: MutableLiveData<Pair<SyncState, Date?>> = MutableLiveData(
        Pair(
            SyncState.FULL,
            null
        )
    )
    override val lastSynchronizedDate: LiveData<Pair<SyncState, Date?>> = _lastSynchronizedDate

    override fun updateLastSynchronizedDate(complete: Boolean) {
        _lastSynchronizedDate.postValue(
            Pair(
                if (complete) SyncState.FULL else SyncState.ESSENTIAL,
                appSyncDao.updateLastSynchronizedDate(complete)
            )
        )
    }

    override fun getLastSynchronizedDate(): Pair<SyncState, Date?> {
        val lastSynchronizedDate = appSyncDao.getLastSynchronizedDate()
        val lastEssentialSynchronizedDate = appSyncDao.getLastEssentialSynchronizedDate()

        if (lastEssentialSynchronizedDate == null) {
            _lastSynchronizedDate.postValue(
                Pair(
                    SyncState.FULL,
                    lastSynchronizedDate
                )
            )

            return Pair(
                SyncState.FULL,
                lastSynchronizedDate
            )
        }

        if (lastSynchronizedDate == null) {
            _lastSynchronizedDate.postValue(
                Pair(
                    SyncState.FULL,
                    null
                )
            )

            return Pair(
                SyncState.FULL,
                null
            )
        }

        _lastSynchronizedDate.postValue(
            Pair(
                if (lastSynchronizedDate.after(lastEssentialSynchronizedDate)) SyncState.FULL else SyncState.ESSENTIAL,
                if (lastSynchronizedDate.after(lastEssentialSynchronizedDate)) lastSynchronizedDate else lastEssentialSynchronizedDate
            )
        )

        return Pair(
            if (lastSynchronizedDate.after(lastEssentialSynchronizedDate)) SyncState.FULL else SyncState.ESSENTIAL,
            if (lastSynchronizedDate.after(lastEssentialSynchronizedDate)) lastSynchronizedDate else lastEssentialSynchronizedDate
        )
    }

    enum class SyncState {
        FULL,
        ESSENTIAL
    }
}
