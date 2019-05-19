package fr.geonature.commons.input

import android.os.Parcel
import android.os.Parcelable

/**
 * Dummy implementation of [AbstractInputTaxon].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DummyInputTaxon : AbstractInputTaxon {

    constructor()
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