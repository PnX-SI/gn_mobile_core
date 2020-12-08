package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.get
import java.util.Locale

/**
 * Describes a taxonomic rank.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(
    tableName = Taxonomy.TABLE_NAME,
    primaryKeys = [Taxonomy.COLUMN_KINGDOM, Taxonomy.COLUMN_GROUP]
)
class Taxonomy : Parcelable {

    @ColumnInfo(name = COLUMN_KINGDOM)
    var kingdom: String

    @ColumnInfo(name = COLUMN_GROUP)
    var group: String

    constructor(
        kingdom: String,
        group: String? = ANY
    ) {
        this.kingdom = sanitizeValue(kingdom)
        this.group = sanitizeValue(group)
    }

    private constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Taxonomy) return false

        if (kingdom != other.kingdom) return false
        if (group != other.group) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kingdom.hashCode()
        result = 31 * result + group.hashCode()

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
            it.writeString(kingdom)
            it.writeString(group)
        }
    }

    override fun toString(): String {
        return "Taxonomy(kingdom='$kingdom', group='$group')"
    }

    fun isAny(): Boolean {
        return kingdom == ANY && group == ANY
    }

    companion object {

        private val TAG = Taxonomy::class.java.name

        /**
         * The name of the 'taxonomy' table.
         */
        const val TABLE_NAME = "taxonomy"

        const val COLUMN_KINGDOM = "kingdom"
        const val COLUMN_GROUP = "group"

        /**
         * The default undefined taxonomy rank as "any".
         */
        const val ANY = "any"

        private val sanitizeValue: (String?) -> String = { value ->
            if (value == null || value.isEmpty() || arrayOf(
                    "autre",
                    "all"
                ).any {
                    value.toLowerCase(Locale.ROOT)
                        .startsWith(it)
                }
            ) ANY
            else value
        }

        /**
         * Gets the default projection.
         */
        fun defaultProjection(tableAlias: String = TABLE_NAME): Array<Pair<String, String>> {
            return arrayOf(
                column(
                    COLUMN_KINGDOM,
                    tableAlias
                ),
                column(
                    COLUMN_GROUP,
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
         * Create a new [Taxonomy] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [Taxonomy] instance
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): Taxonomy? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                Taxonomy(
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_KINGDOM,
                                tableAlias
                            )
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_GROUP,
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
        val CREATOR: Parcelable.Creator<Taxonomy> = object : Parcelable.Creator<Taxonomy> {

            override fun createFromParcel(source: Parcel): Taxonomy {
                return Taxonomy(source)
            }

            override fun newArray(size: Int): Array<Taxonomy?> {
                return arrayOfNulls(size)
            }
        }
    }
}
