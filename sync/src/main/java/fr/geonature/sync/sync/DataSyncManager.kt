package fr.geonature.sync.sync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fr.geonature.sync.data.dao.AppSyncDao
import java.util.Date

/**
 * Manage synchronization statuses.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DataSyncManager private constructor(applicationContext: Context) {

    private val appSyncDao = AppSyncDao(applicationContext)

    private val _lastSynchronizedDate: MutableLiveData<Date?> = MutableLiveData()
    val lastSynchronizedDate: LiveData<Date?> = _lastSynchronizedDate
    val syncMessage: MutableLiveData<String> = MutableLiveData()
    val serverStatus: MutableLiveData<ServerStatus> = MutableLiveData()

    fun updateLastSynchronizedDate() {
        _lastSynchronizedDate.postValue(appSyncDao.updateLastSynchronizedDate())
    }

    fun getLastSynchronizedDate(): Date? {
        val lastSynchronizedDate = appSyncDao.getLastSynchronizedDate()
        _lastSynchronizedDate.postValue(lastSynchronizedDate)

        return lastSynchronizedDate
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
        fun getInstance(applicationContext: Context): DataSyncManager = INSTANCE
            ?: synchronized(this) {
                INSTANCE ?: DataSyncManager(applicationContext).also { INSTANCE = it }
            }
    }
}
