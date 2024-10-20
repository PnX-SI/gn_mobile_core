package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import fr.geonature.compat.os.readParcelableCompat

/**
 * Describes a nomenclature item with type.
 *
 * @author S. Grimault
 */
open class NomenclatureWithType : Nomenclature {

    var type: NomenclatureType? = null

    constructor(
        id: Long,
        code: String,
        hierarchy: String,
        defaultLabel: String,
        typeId: Long,
        type: NomenclatureType? = null
    ) : super(
        id,
        code,
        hierarchy,
        defaultLabel,
        typeId
    ) {
        this.type = type
    }

    constructor(nomenclature: Nomenclature) : super(
        nomenclature.id,
        nomenclature.code,
        nomenclature.hierarchy,
        nomenclature.defaultLabel,
        nomenclature.typeId
    )

    internal constructor(source: Parcel) : super(source) {
        type = source.readParcelableCompat()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NomenclatureWithType) return false
        if (!super.equals(other)) return false

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)

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
            type,
            flags
        )
    }

    companion object {

        /**
         * Gets the default projection.
         */
        fun defaultProjection(): Array<Pair<String, String>> {
            return arrayOf(
                *NomenclatureType.defaultProjection(),
                *Nomenclature.defaultProjection()
            )
        }

        /**
         * Create a new [NomenclatureWithType] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [NomenclatureWithType] instance
         */
        fun fromCursor(cursor: Cursor): NomenclatureWithType? {
            val nomenclature = Nomenclature.fromCursor(cursor)
                ?: return null
            val nomenclatureType = NomenclatureType.fromCursor(cursor)

            return NomenclatureWithType(nomenclature).also {
                if (nomenclature.typeId == nomenclatureType?.id) {
                    it.type = nomenclatureType
                }
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<NomenclatureWithType> =
            object : Parcelable.Creator<NomenclatureWithType> {
                override fun createFromParcel(parcel: Parcel): NomenclatureWithType {
                    return NomenclatureWithType(parcel)
                }

                override fun newArray(size: Int): Array<NomenclatureWithType?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
