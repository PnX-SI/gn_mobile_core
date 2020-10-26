package fr.geonature.sync.data.dao

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.MatrixCursor
import androidx.preference.PreferenceManager
import fr.geonature.commons.data.AppSync
import fr.geonature.commons.data.helper.Converters.dateToTimestamp
import fr.geonature.commons.data.helper.Converters.fromTimestamp
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
import java.util.Date

/**
 * Data access object for [AppSync].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSyncDao(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun findByPackageId(packageId: String?): Cursor {
        val cursor = MatrixCursor(AppSync.defaultProjection().map { it.second }.toTypedArray())

        if (packageId.isNullOrBlank()) return cursor

        val values = arrayOf(
            packageId,
            dateToTimestamp(getLastSynchronizedDate()),
            countInputsToSynchronize(packageId)
        )

        cursor.addRow(values)

        return cursor
    }

    fun updateLastSynchronizedDate(): Date {
        val now = Date()

        this.sharedPreferences.edit()
            .putLong(
                "sync.${AppSync.COLUMN_LAST_SYNC}",
                dateToTimestamp(now) ?: -1L
            )
            .apply()

        return now
    }

    fun getLastSynchronizedDate(): Date? {
        return this.sharedPreferences.getLong(
            "sync.${AppSync.COLUMN_LAST_SYNC}",
            -1L
        )
            .takeUnless { it == -1L }
            .run { fromTimestamp(this) }
    }

    private fun countInputsToSynchronize(packageId: String): Number {
        return FileUtils.getInputsFolder(
            context,
            packageId
        )
            .walkTopDown()
            .filter { it.isFile && it.extension == "json" && it.canRead() }
            .count()
    }
}
