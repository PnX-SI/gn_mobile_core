package fr.geonature.sync.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import java.util.Date

/**
 * Manage synchronization statuses.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DataSyncManager private constructor(applicationContext: Context) {

    private val preferenceManager: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(applicationContext)

    private val _lastSynchronizedDate: MutableLiveData<Date?> = MutableLiveData()
    val lastSynchronizedDate: LiveData<Date?> = _lastSynchronizedDate
    val syncMessage: MutableLiveData<String> = MutableLiveData()
    val serverStatus: MutableLiveData<ServerStatus> = MutableLiveData()

    fun updateLastSynchronizedDate() {
        val now = Date()

        preferenceManager.edit()
            .putLong(
                KEY_PREFERENCE_SYNC_DATE,
                now.time
            )
            .apply()

        _lastSynchronizedDate.postValue(now)
    }

    fun getLastSynchronizedDate(): Date? {
        val timestamp = preferenceManager.getLong(
            KEY_PREFERENCE_SYNC_DATE,
            0
        )

        if (timestamp == 0L) {
            _lastSynchronizedDate.postValue(null)

            return null
        }

        val synchronizedDate = Date(timestamp)
        _lastSynchronizedDate.postValue(synchronizedDate)

        return synchronizedDate
    }

    companion object {

        private const val KEY_PREFERENCE_SYNC_DATE = "key_preference_sync_date"

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
