package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.get
import org.tinylog.Logger

/**
 * Describes a nomenclature item.
 *
 * @author S. Grimault
 */
@Entity(
    tableName = Nomenclature.TABLE_NAME,
    foreignKeys = [ForeignKey(
        entity = NomenclatureType::class,
        parentColumns = [NomenclatureType.COLUMN_ID],
        childColumns = [Nomenclature.COLUMN_TYPE_ID],
        onDelete = ForeignKey.CASCADE
    )]
)
open class Nomenclature(
    /**
     * The unique ID of this nomenclature.
     */
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) var id: Long,

    @ColumnInfo(name = COLUMN_CODE) var code: String,

    @ColumnInfo(name = COLUMN_HIERARCHY) var hierarchy: String,

    @ColumnInfo(name = COLUMN_DEFAULT_LABEL) var defaultLabel: String,

    @ColumnInfo(
        name = COLUMN_TYPE_ID,
        index = true
    ) var typeId: Long
) : Parcelable {

    internal constructor(source: Parcel) : this(
        source.readLong(),
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readLong()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Nomenclature) return false

        if (id != other.id) return false
        if (code != other.code) return false
        if (hierarchy != other.hierarchy) return false
        if (defaultLabel != other.defaultLabel) return false
        if (typeId != other.typeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + hierarchy.hashCode()
        result = 31 * result + defaultLabel.hashCode()
        result = 31 * result + typeId.hashCode()

        return result
    }

    override fun toString(): String {
        return "Nomenclature(id=$id, code='$code', hierarchy='$hierarchy', defaultLabel='$defaultLabel', typeId=$typeId)"
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.also {
            it.writeLong(id)
            it.writeString(code)
            it.writeString(hierarchy)
            it.writeString(defaultLabel)
            it.writeLong(typeId)
        }
    }

    companion object {

        /**
         * The name of the 'nomenclatures' table.
         */
        const val TABLE_NAME = "nomenclatures"

        /**
         * The name of the 'ID' column.
         */
        const val COLUMN_ID = BaseColumns._ID

        const val COLUMN_CODE = "code"
        const val COLUMN_HIERARCHY = "hierarchy"
        const val COLUMN_DEFAULT_LABEL = "default_label"
        const val COLUMN_TYPE_ID = "type_id"

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
                    COLUMN_CODE,
                    tableAlias
                ),
                column(
                    COLUMN_HIERARCHY,
                    tableAlias
                ),
                column(
                    COLUMN_DEFAULT_LABEL,
                    tableAlias
                ),
                column(
                    COLUMN_TYPE_ID,
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
         * Create a new [Nomenclature] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [Nomenclature] instance
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): Nomenclature? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                Nomenclature(
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
                                COLUMN_CODE,
                                tableAlias
                            )
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_HIERARCHY,
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
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_TYPE_ID,
                                tableAlias
                            )
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
        val CREATOR: Parcelable.Creator<Nomenclature> = object : Parcelable.Creator<Nomenclature> {

            override fun createFromParcel(source: Parcel): Nomenclature {
                return Nomenclature(source)
            }

            override fun newArray(size: Int): Array<Nomenclature?> {
                return arrayOfNulls(size)
            }
        }
    }
}
