package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import fr.geonature.commons.util.get
import java.util.Locale

/**
 * Describes a taxonomic rank.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(tableName = Taxonomy.TABLE_NAME,
        primaryKeys = [Taxonomy.COLUMN_KINGDOM, Taxonomy.COLUMN_GROUP])
class Taxonomy : Parcelable {

    @ColumnInfo(name = COLUMN_KINGDOM)
    var kingdom: String
    @ColumnInfo(name = COLUMN_GROUP)
    var group: String

    constructor(kingdom: String,
                group: String) {
        this.kingdom = sanitizeValue(kingdom)
        this.group = sanitizeValue(group)
    }

    private constructor(source: Parcel) : this(source.readString()!!,
                                               source.readString()!!)

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

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeString(kingdom)
        dest?.writeString(group)
    }

    override fun toString(): String {
        return "Taxonomy(kingdom='$kingdom', group='$group')"
    }

    companion object {

        private val TAG = Taxonomy::class.java.name

        /**
         * The name of the 'taxonomy' table.
         */
        const val TABLE_NAME = "taxonomy"

        const val COLUMN_KINGDOM = "kingdom"
        const val COLUMN_GROUP = "group"

        val DEFAULT_PROJECTION = arrayOf(COLUMN_KINGDOM,
                                         COLUMN_GROUP)

        /**
         * The default undefined taxonomy rank as "any".
         */
        val ANY = "any"

        val sanitizeValue: (String) -> String = { value ->
            if (value.isEmpty() || arrayOf("autre",
                                           "all").any {
                    value.toLowerCase(Locale.ROOT)
                        .startsWith(it)
                }) ANY
            else value
        }

        /**
         * Create a new [Taxonomy] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [Taxonomy] instance
         */
        fun fromCursor(cursor: Cursor): Taxonomy? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                Taxonomy(requireNotNull(cursor.get(COLUMN_KINGDOM)),
                         requireNotNull(cursor.get(COLUMN_GROUP)))
            }
            catch (iae: IllegalArgumentException) {
                Log.w(TAG,
                      iae.message)

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
