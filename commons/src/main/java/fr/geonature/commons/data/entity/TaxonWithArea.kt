package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable

/**
 * Describes a taxon with area.
 *
 * @author S. Grimault
 */
class TaxonWithArea : AbstractTaxon {

    var taxonArea: TaxonArea? = null

    constructor(
        id: Long,
        name: String,
        taxonomy: Taxonomy,
        commonName: String? = null,
        description: String? = null,
        rank: String? = null,
        taxonArea: TaxonArea?
    ) : super(
        id,
        name,
        taxonomy,
        commonName,
        description,
        rank
    ) {
        this.taxonArea = taxonArea
    }

    constructor(taxon: Taxon) : super(
        taxon.id,
        taxon.name,
        taxon.taxonomy,
        taxon.commonName,
        taxon.description,
        taxon.rank
    )

    private constructor(source: Parcel) : super(source) {
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

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        super.writeToParcel(
            dest,
            flags
        )

        dest.writeParcelable(
            taxonArea,
            flags
        )
    }

    companion object {

        /**
         * Gets the default projection.
         */
        fun defaultProjection(): Array<Pair<String, String>> {
            return arrayOf(
                *Taxon.defaultProjection(),
                *TaxonArea.defaultProjection()
            )
        }

        /**
         * Create a new [TaxonWithArea] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [TaxonWithArea] instance
         */
        fun fromCursor(cursor: Cursor): TaxonWithArea? {
            val taxon = Taxon.fromCursor(cursor)
                ?: return null
            val taxonArea = TaxonArea.fromCursor(cursor)

            return TaxonWithArea(taxon).also {
                if (taxon.id == taxonArea?.taxonId) {
                    it.taxonArea = taxonArea
                }
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<TaxonWithArea> =
            object : Parcelable.Creator<TaxonWithArea> {
                override fun createFromParcel(parcel: Parcel): TaxonWithArea {
                    return TaxonWithArea(parcel)
                }

                override fun newArray(size: Int): Array<TaxonWithArea?> {
                    return arrayOfNulls(size)
                }
            }
    }

    /**
     * Filter query builder.
     */
    class Filter : AbstractTaxon.Filter(Taxon.TABLE_NAME) {

        /**
         * Filter by area 'colors'.
         *
         * @return this
         */
        fun byAreaColors(vararg color: String): AbstractTaxon.Filter {
            if (color.isEmpty()) {
                return this
            }

            this.wheres.add(
                Pair(
                    "(${
                        getColumnAlias(
                        TaxonArea.COLUMN_COLOR,
                        TaxonArea.TABLE_NAME
                    )
                    } IN (${color.filter { it != "none" }
                        .joinToString(", ") { "'$it'" }})${color.find { it == "none" }
                        ?.let {
                            " OR (${
                                getColumnAlias(
                                TaxonArea.COLUMN_COLOR,
                                TaxonArea.TABLE_NAME
                            )
                            } IS NULL)"
                        } ?: ""})",
                    null
                )
            )

            return this
        }
    }

    /**
     * Order by query builder.
     */
    class OrderBy : AbstractTaxon.OrderBy(Taxon.TABLE_NAME)
}
