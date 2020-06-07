package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.room.Entity
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.get

/**
 * Describes a taxon.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(
    tableName = Taxon.TABLE_NAME,
    primaryKeys = [AbstractTaxon.COLUMN_ID]
)
class Taxon : AbstractTaxon {

    constructor(
        id: Long,
        name: String,
        taxonomy: Taxonomy,
        description: String? = null,
        heritage: Boolean = false
    ) : super(
        id,
        name,
        taxonomy,
        description,
        heritage
    )

    private constructor(source: Parcel) : super(source)

    companion object {

        private val TAG = Taxon::class.java.name

        /**
         * The name of the 'taxa' table.
         */
        const val TABLE_NAME = "taxa"

        /**
         * Gets the default projection.
         */
        fun defaultProjection(tableAlias: String = TABLE_NAME): Array<Pair<String, String>> {
            return AbstractTaxon.defaultProjection(tableAlias)
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
         * Create a new [Taxon] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [Taxon] instance
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): Taxon? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                val taxonomy = Taxonomy.fromCursor(
                    cursor,
                    tableAlias
                ) ?: return null

                Taxon(
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
                                COLUMN_NAME,
                                tableAlias
                            )
                        )
                    ),
                    taxonomy,
                    cursor.get(
                        getColumnAlias(
                            COLUMN_DESCRIPTION,
                            tableAlias
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_HERITAGE,
                                tableAlias
                            ),
                            false
                        )
                    )
                )
            } catch (e: Exception) {
                e.message?.run {
                    Log.w(
                        TAG,
                        this
                    )
                }

                null
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<Taxon> = object : Parcelable.Creator<Taxon> {

            override fun createFromParcel(source: Parcel): Taxon {
                return Taxon(source)
            }

            override fun newArray(size: Int): Array<Taxon?> {
                return arrayOfNulls(size)
            }
        }
    }

    /**
     * Filter query builder.
     */
    class Filter : AbstractTaxon.Filter(TABLE_NAME)
}
