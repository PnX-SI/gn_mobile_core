package fr.geonature.commons.settings

import android.os.Parcel
import android.os.Parcelable

/**
 * Dummy implementation of [IAppSettings].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class DummyAppSettings(var attribute: String? = null) : IAppSettings {

    private constructor(source: Parcel) : this(source.readString())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeString(attribute)
    }

    companion object CREATOR : Parcelable.Creator<DummyAppSettings> {
        override fun createFromParcel(parcel: Parcel): DummyAppSettings {
            return DummyAppSettings(parcel)
        }

        override fun newArray(size: Int): Array<DummyAppSettings?> {
            return arrayOfNulls(size)
        }
    }
}