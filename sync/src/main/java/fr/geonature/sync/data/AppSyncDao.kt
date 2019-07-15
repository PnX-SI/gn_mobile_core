package fr.geonature.sync.data

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.MatrixCursor
import android.preference.PreferenceManager
import fr.geonature.commons.data.AppSync

/**
 * Data access object for [AppSync].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSyncDao(context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun findByPackageId(packageId: String): Cursor {
        val columns = arrayOf(AppSync.COLUMN_ID,
                              AppSync.COLUMN_LAST_SYNC,
                              AppSync.COLUMN_INPUTS_TO_SYNCHRONIZE)
        val cursor = MatrixCursor(columns)
        val lastSync = this.sharedPreferences.getString(
            buildPreferenceKeyFromPackageId(packageId,
                                            AppSync.COLUMN_LAST_SYNC),
            null)
        val inputsToSynchronize = this.sharedPreferences.getLong(
            buildPreferenceKeyFromPackageId(packageId,
                                            AppSync.COLUMN_INPUTS_TO_SYNCHRONIZE),
            0)

        val values = arrayOf(packageId,
                             lastSync,
                             inputsToSynchronize)
        cursor.addRow(values)

        return cursor
    }

    private fun buildPreferenceKeyFromPackageId(packageId: String, key: String): String {
        return "sync.$packageId.$key"
    }
}