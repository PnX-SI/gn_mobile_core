package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.get
import java.util.Date

/**
 * Synchronization status.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class AppSync(
    var packageId: String,
    var lastSync: Date? = null,
    var inputsToSynchronize: Int = 0
) : Parcelable {

    private constructor(source: Parcel) : this(
        source.readString()!!,
        source.readSerializable() as Date,
        source.readInt()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
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
         * Gets the default projection.
         */
        fun defaultProjection(tableAlias: String = TABLE_NAME): Array<Pair<String, String>> {
            return arrayOf(
                column(
                    COLUMN_ID,
                    tableAlias
                ),
                column(
                    COLUMN_LAST_SYNC,
                    tableAlias
                ),
                column(
                    COLUMN_INPUTS_TO_SYNCHRONIZE,
                    tableAlias
                )
            )
        }

        /**
         * Gets alias from given column name.
         */
        fun getColumnAlias(
            columnName: String,
            tableAlias: String = TABLE_NAME
        ): String {
            return column(
                columnName,
                tableAlias
            ).second
        }

        /**
         * Create a new [AppSync] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [AppSync] instance.
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): AppSync? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                AppSync(
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_ID,
                                tableAlias
                            )
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_LAST_SYNC,
                            tableAlias
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_INPUTS_TO_SYNCHRONIZE,
                                tableAlias
                            ),
                            0
                        )
                    )
                )
            } catch (e: Exception) {
                Log.w(
                    TAG,
                    e
                )

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
