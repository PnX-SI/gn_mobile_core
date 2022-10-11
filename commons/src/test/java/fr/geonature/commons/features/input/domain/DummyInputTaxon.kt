package fr.geonature.commons.features.input.domain

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.data.entity.AbstractTaxon

/**
 * Dummy implementation of [AbstractInputTaxon].
 *
 * @author S. Grimault
 */
class DummyInputTaxon : AbstractInputTaxon {

    constructor(taxon: AbstractTaxon) : super(taxon)
    constructor(source: Parcel) : super(source)

    companion object CREATOR : Parcelable.Creator<DummyInputTaxon> {
        override fun createFromParcel(parcel: Parcel): DummyInputTaxon {
            return DummyInputTaxon(parcel)
        }

        override fun newArray(size: Int): Array<DummyInputTaxon?> {
            return arrayOfNulls(size)
        }
    }
}
