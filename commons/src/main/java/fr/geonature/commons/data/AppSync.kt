package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcelable
import fr.geonature.commons.util.IsoDateUtils.toDate
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Synchronization status.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Parcelize
data class AppSync(var packageId: String,
                   var lastSync: Date? = null,
                   var inputsToSynchronize: Number = 0) : Parcelable {

    companion object {
        const val TABLE_NAME = "app_sync"
        const val COLUMN_ID = "package_id"
        const val COLUMN_LAST_SYNC = "last_sync"
        const val COLUMN_INPUTS_TO_SYNCHRONIZE = "inputs_to_synchronize"

        /**
         * Create a new [AppSync] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [AppSync] instance.
         */
        fun fromCursor(cursor: Cursor): AppSync? {
            if (cursor.isClosed) {
                return null
            }

            val appSync = AppSync(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)))
            appSync.lastSync = toDate(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_SYNC)))
            appSync.inputsToSynchronize = cursor.getLong(cursor.getColumnIndexOrThrow(
                    COLUMN_INPUTS_TO_SYNCHRONIZE))

            return appSync
        }
    }
}