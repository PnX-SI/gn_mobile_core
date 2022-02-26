package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.TypeConverters
import fr.geonature.commons.data.helper.Converters
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.get
import org.tinylog.Logger
import java.util.Date

/**
 * Describes a taxon data within an area.
 *
 * @author S. Grimault
 */
@Entity(
    tableName = TaxonArea.TABLE_NAME,
    primaryKeys = [TaxonArea.COLUMN_TAXON_ID, TaxonArea.COLUMN_AREA_ID],
    foreignKeys = [
        ForeignKey(
            entity = Taxon::class,
            parentColumns = [AbstractTaxon.COLUMN_ID],
            childColumns = [TaxonArea.COLUMN_TAXON_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(Converters::class)
data class TaxonArea(

    /**
     * The foreign key taxon ID of taxon.
     */
    @ColumnInfo(name = COLUMN_TAXON_ID)
    var taxonId: Long,

    @ColumnInfo(name = COLUMN_AREA_ID)
    var areaId: Long,

    @ColumnInfo(name = COLUMN_COLOR)
    var color: String?,

    @ColumnInfo(name = COLUMN_NUMBER_OF_OBSERVERS)
    var numberOfObservers: Int,

    @ColumnInfo(name = COLUMN_LAST_UPDATED_AT)
    var lastUpdatedAt: Date?
) : Parcelable {

    private constructor(source: Parcel) : this(
        source.readLong(),
        source.readLong(),
        source.readString(),
        source.readInt(),
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
            it.writeLong(taxonId)
            it.writeLong(areaId)
            it.writeString(color)
            it.writeInt(numberOfObservers)
            it.writeSerializable(lastUpdatedAt)
        }
    }

    companion object {

        /**
         * The name of the 'taxa_area' table.
         */
        const val TABLE_NAME = "taxa_area"

        const val COLUMN_TAXON_ID = "taxon_id"
        const val COLUMN_AREA_ID = "area_id"
        const val COLUMN_COLOR = "color"
        const val COLUMN_NUMBER_OF_OBSERVERS = "nb_observers"
        const val COLUMN_LAST_UPDATED_AT = "last_updated_at"

        /**
         * Gets the default projection.
         */
        fun defaultProjection(tableAlias: String = TABLE_NAME): Array<Pair<String, String>> {
            return arrayOf(
                column(
                    COLUMN_TAXON_ID,
                    tableAlias
                ),
                column(
                    COLUMN_AREA_ID,
                    tableAlias
                ),
                column(
                    COLUMN_COLOR,
                    tableAlias
                ),
                column(
                    COLUMN_NUMBER_OF_OBSERVERS,
                    tableAlias
                ),
                column(
                    COLUMN_LAST_UPDATED_AT,
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
         * Create a new [TaxonArea] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [TaxonArea] instance
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): TaxonArea? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                TaxonArea(
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_TAXON_ID,
                                tableAlias
                            )
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_AREA_ID,
                                tableAlias
                            )
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_COLOR,
                            tableAlias
                        ),
                        "#00000000"
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_NUMBER_OF_OBSERVERS,
                                tableAlias
                            ),
                            0
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_LAST_UPDATED_AT,
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
        val CREATOR: Parcelable.Creator<TaxonArea> = object : Parcelable.Creator<TaxonArea> {

            override fun createFromParcel(source: Parcel): TaxonArea {
                return TaxonArea(source)
            }

            override fun newArray(size: Int): Array<TaxonArea?> {
                return arrayOfNulls(size)
            }
        }
    }
}
