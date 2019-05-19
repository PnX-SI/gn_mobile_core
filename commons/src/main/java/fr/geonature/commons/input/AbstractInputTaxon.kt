package fr.geonature.commons.input

import android.os.Parcel
import android.os.Parcelable

/**
 * Describes an input taxon.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see fr.geonature.commons.data.Taxon
 */
abstract class AbstractInputTaxon: Parcelable {
    var id: Long = 0

    constructor()

    constructor(source: Parcel) {
        this.id = source.readLong()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel,
                               flags: Int) {
        dest.writeLong(this.id)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractInputTaxon

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}