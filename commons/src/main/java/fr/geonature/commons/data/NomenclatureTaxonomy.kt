package fr.geonature.commons.data

import android.database.Cursor
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.get

/**
 * Describes a nomenclature item with taxonomy as join table.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Entity(
    tableName = NomenclatureTaxonomy.TABLE_NAME,
    primaryKeys = [NomenclatureTaxonomy.COLUMN_NOMENCLATURE_ID, Taxonomy.COLUMN_KINGDOM, Taxonomy.COLUMN_GROUP],
    indices = [Index(value = [Taxonomy.COLUMN_KINGDOM, Taxonomy.COLUMN_GROUP])],
    foreignKeys = [ForeignKey(
        entity = Nomenclature::class,
        parentColumns = [Nomenclature.COLUMN_ID],
        childColumns = [NomenclatureTaxonomy.COLUMN_NOMENCLATURE_ID],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Taxonomy::class,
        parentColumns = [Taxonomy.COLUMN_KINGDOM, Taxonomy.COLUMN_GROUP],
        childColumns = [Taxonomy.COLUMN_KINGDOM, Taxonomy.COLUMN_GROUP],
        onDelete = ForeignKey.CASCADE
    )]
)
class NomenclatureTaxonomy(
    @ColumnInfo(name = COLUMN_NOMENCLATURE_ID)
    var nomenclatureId: Long,
    @Embedded
    var taxonomy: Taxonomy
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NomenclatureTaxonomy) return false

        if (nomenclatureId != other.nomenclatureId) return false
        if (taxonomy != other.taxonomy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nomenclatureId.hashCode()
        result = 31 * result + taxonomy.hashCode()

        return result
    }

    companion object {

        private val TAG = Nomenclature::class.java.name

        /**
         * The name of the 'nomenclatures_taxonomy' table.
         */
        const val TABLE_NAME = "nomenclatures_taxonomy"

        const val COLUMN_NOMENCLATURE_ID = "nomenclature_id"

        /**
         * Gets the default projection.
         */
        fun defaultProjection(tableAlias: String = TABLE_NAME): Array<Pair<String, String>> {
            return arrayOf(
                column(
                    COLUMN_NOMENCLATURE_ID,
                    tableAlias
                ),
                column(
                    Taxonomy.COLUMN_KINGDOM,
                    tableAlias
                ),
                column(
                    Taxonomy.COLUMN_GROUP,
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
         * Create a new [NomenclatureTaxonomy] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [NomenclatureTaxonomy] instance
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): NomenclatureTaxonomy? {
            if (cursor.isClosed) {
                return null
            }

            val taxonomy = Taxonomy.fromCursor(
                cursor,
                tableAlias
            ) ?: return null

            return try {
                NomenclatureTaxonomy(
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_NOMENCLATURE_ID,
                                tableAlias
                            )
                        )
                    ),
                    taxonomy
                )
            } catch (e: Exception) {
                Log.w(
                    TAG,
                    e
                )

                null
            }
        }
    }
}
