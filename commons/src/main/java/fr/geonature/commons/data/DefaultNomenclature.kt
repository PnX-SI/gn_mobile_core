package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.get

/**
 * Describes a default nomenclature item from given module.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(
    tableName = DefaultNomenclature.TABLE_NAME,
    primaryKeys = [DefaultNomenclature.COLUMN_MODULE, DefaultNomenclature.COLUMN_NOMENCLATURE_ID],
    foreignKeys = [ForeignKey(
        entity = Nomenclature::class,
        parentColumns = [Nomenclature.COLUMN_ID],
        childColumns = [DefaultNomenclature.COLUMN_NOMENCLATURE_ID],
        onDelete = ForeignKey.CASCADE
    )]
)
open class DefaultNomenclature(
    @ColumnInfo(name = COLUMN_MODULE)
    var module: String,

    @ColumnInfo(
        name = COLUMN_NOMENCLATURE_ID,
        index = true
    )
    var nomenclatureId: Long
) : Parcelable {

    internal constructor(source: Parcel) : this(
        source.readString()!!,
        source.readLong()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefaultNomenclature) return false

        if (module != other.module) return false
        if (nomenclatureId != other.nomenclatureId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = module.hashCode()
        result = 31 * result + nomenclatureId.hashCode()

        return result
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeString(module)
            it.writeLong(nomenclatureId)
        }
    }

    companion object {

        private val TAG = DefaultNomenclature::class.java.name

        /**
         * The name of the 'default_nomenclatures' table.
         */
        const val TABLE_NAME = "default_nomenclatures"

        const val COLUMN_MODULE = "module"
        const val COLUMN_NOMENCLATURE_ID = "nomenclature_id"

        /**
         * Gets the default projection.
         */
        fun defaultProjection(tableAlias: String = TABLE_NAME): Array<Pair<String, String>> {
            return arrayOf(
                column(
                    COLUMN_MODULE,
                    tableAlias
                ),
                column(
                    COLUMN_NOMENCLATURE_ID,
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
         * Create a new [DefaultNomenclature] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [DefaultNomenclature] instance
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): DefaultNomenclature? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                DefaultNomenclature(
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
                                COLUMN_NOMENCLATURE_ID,
                                tableAlias
                            )
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
        val CREATOR: Parcelable.Creator<DefaultNomenclature> = object :
            Parcelable.Creator<DefaultNomenclature> {

            override fun createFromParcel(source: Parcel): DefaultNomenclature {
                return DefaultNomenclature(source)
            }

            override fun newArray(size: Int): Array<DefaultNomenclature?> {
                return arrayOfNulls(size)
            }
        }
    }
}
