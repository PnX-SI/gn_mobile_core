package fr.geonature.commons.data.model

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable

/**
 * Describes a default nomenclature item with nomenclature type from given module.
 *
 * @author S. Grimault
 */
class DefaultNomenclatureWithType : DefaultNomenclature {

    var nomenclatureWithType: NomenclatureWithType? = null

    constructor(
        module: String,
        nomenclatureId: Long,
        nomenclatureWithType: NomenclatureWithType? = null
    ) : super(
        module,
        nomenclatureId
    ) {
        this.nomenclatureWithType = nomenclatureWithType
    }

    constructor(defaultNomenclature: DefaultNomenclature) : super(
        defaultNomenclature.module,
        defaultNomenclature.nomenclatureId
    )

    private constructor(source: Parcel) : super(source) {
        nomenclatureWithType = source.readParcelable(Nomenclature::class.java.classLoader)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefaultNomenclatureWithType) return false
        if (!super.equals(other)) return false

        if (nomenclatureWithType != other.nomenclatureWithType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (nomenclatureWithType?.hashCode() ?: 0)

        return result
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        super.writeToParcel(
            dest,
            flags
        )

        dest?.writeParcelable(
            nomenclatureWithType,
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
                *DefaultNomenclature.defaultProjection()
            )
        }

        /**
         * Create a new [DefaultNomenclatureWithType] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [DefaultNomenclatureWithType] instance
         */
        fun fromCursor(cursor: Cursor): DefaultNomenclatureWithType? {
            val defaultNomenclature = DefaultNomenclature.fromCursor(cursor)
                ?: return null
            val nomenclatureWithType = NomenclatureWithType.fromCursor(cursor)

            return DefaultNomenclatureWithType(defaultNomenclature).also {
                it.nomenclatureWithType = nomenclatureWithType
            }
        }

        @JvmField
        val CREATOR: Parcelable.Creator<DefaultNomenclatureWithType> =
            object : Parcelable.Creator<DefaultNomenclatureWithType> {
                override fun createFromParcel(parcel: Parcel): DefaultNomenclatureWithType {
                    return DefaultNomenclatureWithType(parcel)
                }

                override fun newArray(size: Int): Array<DefaultNomenclatureWithType?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
