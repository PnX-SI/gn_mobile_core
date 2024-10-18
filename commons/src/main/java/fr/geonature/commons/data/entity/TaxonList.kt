package fr.geonature.commons.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import fr.geonature.commons.data.helper.EntityHelper.column
import kotlinx.parcelize.Parcelize

/**
 * Describes a taxon linked with list.
 *
 * @author S. Grimault
 */
@Entity(
    tableName = TaxonList.TABLE_NAME,
    primaryKeys = [TaxonList.COLUMN_TAXON_ID, TaxonList.COLUMN_TAXA_LIST_ID],
    foreignKeys = [ForeignKey(
        entity = Taxon::class,
        parentColumns = [AbstractTaxon.COLUMN_ID],
        childColumns = [TaxonList.COLUMN_TAXON_ID],
        onDelete = ForeignKey.CASCADE
    )]
)
@Parcelize
data class TaxonList(
    /**
     * The foreign key taxon ID of taxon.
     */
    @ColumnInfo(name = COLUMN_TAXON_ID) var taxonId: Long,

    /**
     * The list id.
     */
    @ColumnInfo(name = COLUMN_TAXA_LIST_ID) var taxaListId: Long,
) : Parcelable {

    companion object {

        /**
         * The name of the 'taxa_list' table.
         */
        const val TABLE_NAME = "taxa_list"

        const val COLUMN_TAXON_ID = "taxon_id"
        const val COLUMN_TAXA_LIST_ID = "taxa_list_id"

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
                    COLUMN_TAXA_LIST_ID,
                    tableAlias
                )
            )
        }
    }
}
