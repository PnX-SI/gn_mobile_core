package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable

/**
 * Describes a taxon with area.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxonWithArea : AbstractTaxon {

    var taxonArea: TaxonArea? = null

    constructor(id: Long,
                name: String,
                description: String?,
                heritage: Boolean = false,
                taxonArea: TaxonArea?) : super(id,
                                               name,
                                               description,
                                               heritage) {
        this.taxonArea = taxonArea
    }

    constructor(taxon: Taxon) : super(taxon.id,
                                      taxon.name,
                                      taxon.description,
                                      taxon.heritage)

    constructor(source: Parcel) : super(source) {
        taxonArea = source.readParcelable(TaxonArea::class.java.classLoader)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TaxonWithArea) return false
        if (!super.equals(other)) return false

        if (taxonArea != other.taxonArea) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (taxonArea?.hashCode() ?: 0)

        return result
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        super.writeToParcel(dest,
                            flags)

        dest?.writeParcelable(taxonArea,
                              flags)
    }

    companion object {

        /**
         * Create a new [TaxonWithArea] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [TaxonWithArea] instance
         */
        fun fromCursor(cursor: Cursor): TaxonWithArea? {
            val taxon = Taxon.fromCursor(cursor) ?: return null

            return TaxonWithArea(taxon).apply { taxonArea = TaxonArea.fromCursor(cursor) }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<TaxonWithArea> = object : Parcelable.Creator<TaxonWithArea> {
            override fun createFromParcel(parcel: Parcel): TaxonWithArea {
                return TaxonWithArea(parcel)
            }

            override fun newArray(size: Int): Array<TaxonWithArea?> {
                return arrayOfNulls(size)
            }
        }
    }
}