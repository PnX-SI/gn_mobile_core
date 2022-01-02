package fr.geonature.commons.data.dao

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.MatrixCursor
import androidx.preference.PreferenceManager
import fr.geonature.commons.data.helper.Converters.dateToTimestamp
import fr.geonature.commons.data.helper.Converters.fromTimestamp
import fr.geonature.commons.data.entity.AppSync
import java.util.Date

/**
 * Data access object for [AppSync].
 *
 * @author S. Grimault
 */
class AppSyncDao(context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val inputDao: InputDao = InputDao(context)

    fun findByPackageId(packageId: String?): Cursor {
        val cursor = MatrixCursor(
            AppSync
            .defaultProjection()
            .map { it.second }
            .toTypedArray())

        if (packageId.isNullOrBlank()) return cursor

        val values = arrayOf(
            packageId,
            dateToTimestamp(getLastSynchronizedDate()),
            dateToTimestamp(getLastEssentialSynchronizedDate()),
            inputDao.countInputsToSynchronize(packageId)
        )

        cursor.addRow(values)

        return cursor
    }

    fun updateLastSynchronizedDate(complete: Boolean = true): Date {
        val now = Date()

        this.sharedPreferences
            .edit()
            .putLong(
                buildLastSynchronizedDatePreferenceKey(complete),
                dateToTimestamp(now)
                    ?: -1L
            )
            .apply()

        return now
    }

    fun getLastSynchronizedDate(): Date? {
        return this.sharedPreferences
            .getLong(
                buildLastSynchronizedDatePreferenceKey(),
                -1L
            )
            .takeUnless { it == -1L }
            .run { fromTimestamp(this) }
    }

    fun getLastEssentialSynchronizedDate(): Date? {
        return this.sharedPreferences
            .getLong(
                buildLastSynchronizedDatePreferenceKey(false),
                -1L
            )
            .takeUnless { it == -1L }
            .run { fromTimestamp(this) }
    }

    private fun buildLastSynchronizedDatePreferenceKey(complete: Boolean = true): String {
        return "sync.${if (complete) AppSync.COLUMN_LAST_SYNC else AppSync.COLUMN_LAST_SYNC_ESSENTIAL}"
    }
}
