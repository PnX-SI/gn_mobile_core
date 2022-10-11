package fr.geonature.commons.features.input.domain

import android.os.Parcel
import android.os.Parcelable

/**
 * Dummy implementation of [AbstractInput].
 *
 * @author S. Grimault
 */
class DummyInput : AbstractInput {

    constructor() : super("dummy")
    constructor(source: Parcel) : super(source)

    override fun getTaxaFromParcel(source: Parcel): List<AbstractInputTaxon> {
        val inputTaxa = source.createTypedArrayList(DummyInputTaxon)
        return inputTaxa
            ?: emptyList()
    }

    companion object CREATOR : Parcelable.Creator<DummyInput> {
        override fun createFromParcel(parcel: Parcel): DummyInput {
            return DummyInput(parcel)
        }

        override fun newArray(size: Int): Array<DummyInput?> {
            return arrayOfNulls(size)
        }
    }
}
