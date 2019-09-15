package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.util.IsoDateUtils.toDate
import java.util.Date

/**
 * Synchronization status.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class AppSync(var packageId: String?,
                   var lastSync: Date? = null,
                   var inputsToSynchronize: Int = 0) : Parcelable {

    private constructor(source: Parcel) : this(source.readString(),
                                               source.readSerializable() as Date,
                                               source.readInt())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeString(packageId)
        dest?.writeSerializable(lastSync)
        dest?.writeInt(inputsToSynchronize)
    }

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
            appSync.lastSync = toDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_SYNC)))
            appSync.inputsToSynchronize = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INPUTS_TO_SYNCHRONIZE))

            return appSync
        }

        @JvmField
        val CREATOR: Parcelable.Creator<AppSync> = object : Parcelable.Creator<AppSync> {

            override fun createFromParcel(source: Parcel): AppSync {
                return AppSync(source)
            }

            override fun newArray(size: Int): Array<AppSync?> {
                return arrayOfNulls(size)
            }
        }
    }
}