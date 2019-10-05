package fr.geonature.commons.input

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.data.AbstractTaxon

/**
 * Describes an input taxon.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see AbstractTaxon
 */
abstract class AbstractInputTaxon : Parcelable {

    var taxon: AbstractTaxon

    constructor(taxon: AbstractTaxon) {
        this.taxon = taxon
    }

    constructor(source: Parcel) {
        this.taxon = source.readParcelable(AbstractTaxon::class.java.classLoader)!!
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractInputTaxon) return false

        if (taxon != other.taxon) return false

        return true
    }

    override fun hashCode(): Int {
        return taxon.hashCode()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeParcelable(taxon,
                              flags)
    }
}