package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverters
import fr.geonature.commons.data.helper.Converters
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.get
import org.tinylog.Logger
import java.util.Date

/**
 * Describes a dataset.
 *
 * @author S. Grimault
 */
@Entity(
    tableName = Dataset.TABLE_NAME,
    primaryKeys = [Dataset.COLUMN_ID, Dataset.COLUMN_MODULE]
)
@TypeConverters(Converters::class)
data class Dataset(

    /**
     * The unique ID of this dataset.
     */
    @ColumnInfo(name = COLUMN_ID)
    var id: Long,

    /**
     * The related module of this dataset.
     */
    @ColumnInfo(name = COLUMN_MODULE)
    var module: String,

    /**
     * The name of the dataset.
     */
    @ColumnInfo(name = COLUMN_NAME)
    var name: String,

    /**
     * The description of the dataset.
     */
    @ColumnInfo(name = COLUMN_DESCRIPTION)
    var description: String?,

    /**
     * Whether this dataset is active or not.
     */
    @ColumnInfo(name = COLUMN_ACTIVE)
    var active: Boolean = false,

    /**
     * The creation date of this dataset.
     */
    @ColumnInfo(name = COLUMN_CREATED_AT)
    var createdAt: Date?
) : Parcelable {

    private constructor(source: Parcel) : this(
        source.readLong(),
        source.readString()!!,
        source.readString()!!,
        source.readString(),
        source.readByte() == 1.toByte(),
        source.readSerializable() as Date
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeLong(id)
            it.writeString(module)
            it.writeString(name)
            it.writeString(description)
            it.writeByte((if (active) 1 else 0).toByte()) // as boolean value
            it.writeSerializable(createdAt)
        }
    }

    companion object {

        /**
         * The name of the 'observers' table.
         */
        const val TABLE_NAME = "dataset"

        /**
         * The name of the 'ID' column.
         */
        const val COLUMN_ID = BaseColumns._ID

        const val COLUMN_MODULE = "module"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_ACTIVE = "active"
        const val COLUMN_CREATED_AT = "created_at"

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
                    COLUMN_MODULE,
                    tableAlias
                ),
                column(
                    COLUMN_NAME,
                    tableAlias
                ),
                column(
                    COLUMN_DESCRIPTION,
                    tableAlias
                ),
                column(
                    COLUMN_ACTIVE,
                    tableAlias
                ),
                column(
                    COLUMN_CREATED_AT,
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
         * Create a new [Dataset] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [Dataset] instance
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): Dataset? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                Dataset(
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_ID,
                                tableAlias
                            )
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_MODULE,
                                tableAlias
                            )
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_NAME,
                                tableAlias
                            )
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_DESCRIPTION,
                            tableAlias
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_ACTIVE,
                                tableAlias
                            ),
                            false
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_CREATED_AT,
                            tableAlias
                        )
                    )
                )
            } catch (e: Exception) {
                e.message?.run {
                    Logger.warn { this }
                }

                null
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Dataset> = object : Parcelable.Creator<Dataset> {

            override fun createFromParcel(source: Parcel): Dataset {
                return Dataset(source)
            }

            override fun newArray(size: Int): Array<Dataset?> {
                return arrayOfNulls(size)
            }
        }
    }
}
