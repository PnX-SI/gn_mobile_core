package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import fr.geonature.commons.util.get
import java.util.Date

/**
 * Synchronization status.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class AppSync(var packageId: String,
                   var lastSync: Date? = null,
                   var inputsToSynchronize: Int = 0) : Parcelable {

    private constructor(source: Parcel) : this(source.readString()!!,
                                               source.readSerializable() as Date,
                                               source.readInt())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.also {
            it.writeString(packageId)
            it.writeSerializable(lastSync)
            it.writeInt(inputsToSynchronize)
        }
    }

    companion object {

        private val TAG = AppSync::class.java.name

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

            return try {
                AppSync(requireNotNull(cursor.get(COLUMN_ID)),
                        cursor.get(COLUMN_LAST_SYNC),
                        requireNotNull(cursor.get(COLUMN_INPUTS_TO_SYNCHRONIZE,
                                                  0)))
            }
            catch (iae: IllegalArgumentException) {
                Log.w(TAG,
                      iae.message)

                null
            }
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