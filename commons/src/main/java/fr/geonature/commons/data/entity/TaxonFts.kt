package fr.geonature.commons.data.entity

import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import fr.geonature.commons.data.entity.AbstractTaxon.Companion.COLUMN_DESCRIPTION
import fr.geonature.commons.data.entity.AbstractTaxon.Companion.COLUMN_ID
import fr.geonature.commons.data.entity.AbstractTaxon.Companion.COLUMN_NAME
import fr.geonature.commons.data.entity.AbstractTaxon.Companion.COLUMN_NAME_COMMON
import kotlinx.parcelize.Parcelize

/**
 * Describes a taxon using FTS virtual table.
 *
 * @author S. Grimault
 */
@Fts4(
    contentEntity = Taxon::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
    tokenizerArgs = ["remove_diacritics=2"]
)
@Entity(tableName = TaxonFts.TABLE_NAME)
@Parcelize
@RequiresApi(api = 30)
data class TaxonFts(

    /**
     * The unique ID of the taxon.
     */
    @ColumnInfo(name = COLUMN_ID) val id: Long,

    /**
     * The default name of the taxon.
     */
    @ColumnInfo(name = COLUMN_NAME) val name: String,

    @Embedded val taxonomy: Taxonomy,

    /**
     * The common name of the taxon.
     */
    @ColumnInfo(name = COLUMN_NAME_COMMON) val commonName: String? = null,

    /**
     * The description of the taxon.
     */
    @ColumnInfo(name = COLUMN_DESCRIPTION) val description: String? = null
) : Parcelable {

    companion object {

        /**
         * The name of the 'taxa_fts' table.
         */
        const val TABLE_NAME = "taxa_fts"

        /**
         * Creates a [TaxonFts] from a [Taxon].
         */
        fun from(taxon: Taxon): TaxonFts {
            return TaxonFts(
                id = taxon.id,
                name = taxon.name,
                taxonomy = taxon.taxonomy,
                commonName = taxon.commonName
            )
        }
    }
}