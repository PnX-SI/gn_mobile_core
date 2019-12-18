package fr.geonature.sync.data.dao

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.MatrixCursor
import android.preference.PreferenceManager
import fr.geonature.commons.data.AppSync
import fr.geonature.commons.util.FileUtils.getInputsFolder

/**
 * Data access object for [AppSync].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSyncDao(private val context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun findByPackageId(packageId: String?): Cursor {
        val cursor = MatrixCursor(AppSync.defaultProjection().map { it.second }.toTypedArray())

        if (packageId.isNullOrBlank()) return cursor

        val lastSync = this.sharedPreferences.getString("sync.$packageId.${AppSync.COLUMN_LAST_SYNC}",
                                                        null)

        val values = arrayOf(packageId,
                             lastSync,
                             countInputsToSynchronize(packageId))

        cursor.addRow(values)

        return cursor
    }

    private fun countInputsToSynchronize(packageId: String): Number {
        return getInputsFolder(context,
                               packageId).walkTopDown()
                .filter { it.extension == "json" }
                .count()
    }
}