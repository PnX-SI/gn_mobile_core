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

    private val _lastSynchronizedDate: MutableLiveData<Pair<IDataSyncManager.SyncState, Date?>> =
        MutableLiveData(
            Pair(
                IDataSyncManager.SyncState.FULL,
                null
            )
        )
    override val lastSynchronizedDate: LiveData<Pair<IDataSyncManager.SyncState, Date?>> =
        _lastSynchronizedDate

    override fun updateLastSynchronizedDate(complete: Boolean) {
        _lastSynchronizedDate.postValue(
            Pair(
                if (complete) IDataSyncManager.SyncState.FULL else IDataSyncManager.SyncState.ESSENTIAL,
                appSyncDao.updateLastSynchronizedDate(complete)
            )
        )
    }

    override fun getLastSynchronizedDate(): Pair<IDataSyncManager.SyncState, Date?> {
        val lastSynchronizedDate = appSyncDao.getLastSynchronizedDate()
        val lastEssentialSynchronizedDate = appSyncDao.getLastEssentialSynchronizedDate()

        if (lastEssentialSynchronizedDate == null) {
            _lastSynchronizedDate.postValue(
                Pair(
                    IDataSyncManager.SyncState.FULL,
                    lastSynchronizedDate
                )
            )

            return Pair(
                IDataSyncManager.SyncState.FULL,
                lastSynchronizedDate
            )
        }

        if (lastSynchronizedDate == null) {
            _lastSynchronizedDate.postValue(
                Pair(
                    IDataSyncManager.SyncState.FULL,
                    null
                )
            )

            return Pair(
                IDataSyncManager.SyncState.FULL,
                null
            )
        }

        _lastSynchronizedDate.postValue(
            Pair(
                if (lastSynchronizedDate.after(lastEssentialSynchronizedDate)) IDataSyncManager.SyncState.FULL else IDataSyncManager.SyncState.ESSENTIAL,
                if (lastSynchronizedDate.after(lastEssentialSynchronizedDate)) lastSynchronizedDate else lastEssentialSynchronizedDate
            )
        )

        return Pair(
            if (lastSynchronizedDate.after(lastEssentialSynchronizedDate)) IDataSyncManager.SyncState.FULL else IDataSyncManager.SyncState.ESSENTIAL,
            if (lastSynchronizedDate.after(lastEssentialSynchronizedDate)) lastSynchronizedDate else lastEssentialSynchronizedDate
        )
    }
}
