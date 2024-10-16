package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import fr.geonature.compat.os.readParcelableCompat

/**
 * Describes a nomenclature item with taxonomy.
 *
 * @author S. Grimault
 */
class NomenclatureWithTaxonomy : NomenclatureWithType {

    var taxonony: Taxonomy? = null

    constructor(nomenclatureWithType: NomenclatureWithType) : super(
        nomenclatureWithType.id,
        nomenclatureWithType.code,
        nomenclatureWithType.hierarchy,
        nomenclatureWithType.defaultLabel,
        nomenclatureWithType.typeId,
        nomenclatureWithType.type
    )

    internal constructor(source: Parcel) : super(source) {
        taxonony = source.readParcelableCompat()
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

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        super.writeToParcel(
            dest,
            flags
        )

        dest.writeParcelable(
            taxonony,
            flags
        )
    }

    companion object {

        /**
         * Gets the default projection.
         */
        fun defaultProjection(): Array<Pair<String, String>> {
            return arrayOf(
                *NomenclatureWithType.defaultProjection(),
                *NomenclatureTaxonomy.defaultProjection()
            )
        }

        /**
         * Create a new [NomenclatureWithTaxonomy] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [NomenclatureWithTaxonomy] instance
         */
        fun fromCursor(cursor: Cursor): NomenclatureWithTaxonomy? {
            val nomenclatureWithType = NomenclatureWithType.fromCursor(cursor)
                ?: return null
            val nomenclatureTaxonomy = NomenclatureTaxonomy.fromCursor(cursor)

            return NomenclatureWithTaxonomy(nomenclatureWithType).also {
                if (nomenclatureWithType.id == nomenclatureTaxonomy?.nomenclatureId) {
                    it.taxonony = nomenclatureTaxonomy.taxonomy
                }
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<NomenclatureWithTaxonomy> =
            object : Parcelable.Creator<NomenclatureWithTaxonomy> {
                override fun createFromParcel(parcel: Parcel): NomenclatureWithTaxonomy {
                    return NomenclatureWithTaxonomy(parcel)
                }

                override fun newArray(size: Int): Array<NomenclatureWithTaxonomy?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
