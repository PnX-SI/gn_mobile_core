package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable

/**
 * Describes a nomenclature item with taxonomy.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class NomenclatureWithTaxonomy : Nomenclature {

    var taxonony: Taxonomy? = null

    constructor(id: Long,
                code: String,
                hierarchy: String,
                defaultLabel: String,
                typeId: Long,
                taxonomy: Taxonomy? = null) : super(id,
                                                    code,
                                                    hierarchy,
                                                    defaultLabel,
                                                    typeId) {
        this.taxonony = taxonomy
    }

    constructor(nomenclature: Nomenclature) : super(nomenclature._id,
                                                    nomenclature.code,
                                                    nomenclature.hierarchy,
                                                    nomenclature.defaultLabel,
                                                    nomenclature.typeId)


    private constructor(source: Parcel) : super(source) {
        taxonony = source.readParcelable(Taxonomy::class.java.classLoader)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NomenclatureWithTaxonomy) return false
        if (!super.equals(other)) return false

        if (taxonony != other.taxonony) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (taxonony?.hashCode() ?: 0)

        return result
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        super.writeToParcel(dest,
                            flags)

        dest?.writeParcelable(taxonony,
                              flags)
    }

    companion object {

        val DEFAULT_PROJECTION = arrayOf(NomenclatureType.COLUMN_MNEMONIC,
                                         *Nomenclature.DEFAULT_PROJECTION,
                                         *Taxonomy.DEFAULT_PROJECTION)

        /**
         * Create a new [NomenclatureWithTaxonomy] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [NomenclatureWithTaxonomy] instance
         */
        fun fromCursor(cursor: Cursor): NomenclatureWithTaxonomy? {
            val nomenclature = Nomenclature.fromCursor(cursor) ?: return null
            val nomenclatureTaxonomy = NomenclatureTaxonomy.fromCursor(cursor)

            return NomenclatureWithTaxonomy(nomenclature).also {
                if (nomenclature._id == nomenclatureTaxonomy?.nomenclatureId) {
                    it.taxonony = nomenclatureTaxonomy.taxonomy
                }
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<NomenclatureWithTaxonomy> = object : Parcelable.Creator<NomenclatureWithTaxonomy> {
            override fun createFromParcel(parcel: Parcel): NomenclatureWithTaxonomy {
                return NomenclatureWithTaxonomy(parcel)
            }

            override fun newArray(size: Int): Array<NomenclatureWithTaxonomy?> {
                return arrayOfNulls(size)
            }
        }
    }
}