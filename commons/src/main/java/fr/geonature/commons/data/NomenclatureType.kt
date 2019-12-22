package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.get

/**
 * Describes a nomenclature type.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(
    tableName = NomenclatureType.TABLE_NAME,
    indices = [Index(
        value = [NomenclatureType.COLUMN_MNEMONIC],
        unique = true
    )]
)
data class NomenclatureType(

    /**
     * The unique ID of this nomenclature type.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = COLUMN_ID)
    var id: Long,

    @ColumnInfo(name = COLUMN_MNEMONIC)
    var mnemonic: String,

    @ColumnInfo(name = COLUMN_DEFAULT_LABEL)
    var defaultLabel: String
) : Parcelable {

    private constructor(source: Parcel) : this(
        source.readLong(),
        source.readString()!!,
        source.readString()!!
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
            it.writeString(mnemonic)
            it.writeString(defaultLabel)
        }
    }

    companion object {

        private val TAG = NomenclatureType::class.java.name

        /**
         * The name of the 'nomenclature_types' table.
         */
        const val TABLE_NAME = "nomenclature_types"

        /**
         * The name of the 'ID' column.
         */
        const val COLUMN_ID = BaseColumns._ID

        const val COLUMN_MNEMONIC = "mnemonic"
        const val COLUMN_DEFAULT_LABEL = "default_label"

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
                    COLUMN_MNEMONIC,
                    tableAlias
                ),
                column(
                    COLUMN_DEFAULT_LABEL,
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
         * Create a new [NomenclatureType] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [NomenclatureType] instance
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): NomenclatureType? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                NomenclatureType(
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
                                COLUMN_MNEMONIC,
                                tableAlias
                            )
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_DEFAULT_LABEL,
                                tableAlias
                            )
                        )
                    )
                )
            } catch (e: Exception) {
                Log.w(
                    TAG,
                    e.message
                )

                null
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<NomenclatureType> =
            object : Parcelable.Creator<NomenclatureType> {

                override fun createFromParcel(source: Parcel): NomenclatureType {
                    return NomenclatureType(source)
                }

                override fun newArray(size: Int): Array<NomenclatureType?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
