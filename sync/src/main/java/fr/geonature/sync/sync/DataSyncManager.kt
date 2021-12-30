package fr.geonature.sync.sync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.geonature.commons.data.dao.AppSyncDao
import java.util.Date

/**
 * Manage synchronization statuses.
 *
 * @author S. Grimault
 */
class DataSyncManager private constructor(applicationContext: Context) {

    private val appSyncDao = AppSyncDao(applicationContext)

    private val _lastSynchronizedDate: MutableLiveData<Pair<SyncState, Date?>> = MutableLiveData(
        Pair(
            SyncState.FULL,
            null
        )
    )
    val lastSynchronizedDate: LiveData<Pair<SyncState, Date?>> = _lastSynchronizedDate

    fun updateLastSynchronizedDate(complete: Boolean = true) {
        _lastSynchronizedDate.postValue(
            Pair(
                if (complete) SyncState.FULL else SyncState.ESSENTIAL,
                appSyncDao.updateLastSynchronizedDate(complete)
            )
        )
    }

    fun getLastSynchronizedDate(): Pair<SyncState, Date?> {
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

    companion object {

        @Volatile
        private var INSTANCE: DataSyncManager? = null

        /**
         * Gets the singleton instance of [DataSyncManager].
         *
         * @param applicationContext The main application context.
         *
         * @return The singleton instance of [DataSyncManager].
         */
        fun getInstance(applicationContext: Context): DataSyncManager =
            INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: DataSyncManager(applicationContext).also { INSTANCE = it }
                }
    }
}
