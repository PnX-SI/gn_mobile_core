package fr.geonature.sync.settings

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.settings.IAppSettings
import fr.geonature.datasync.settings.DataSyncSettings

/**
 * Global internal settings.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class AppSettings(var sync: DataSyncSettings? = null) : IAppSettings {

    private constructor(source: Parcel) : this(source.readParcelable(DataSyncSettings::class.java.classLoader))

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeParcelable(
                sync,
                0
            )
        }
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 10000

        @JvmField
        val CREATOR: Parcelable.Creator<AppSettings> = object : Parcelable.Creator<AppSettings> {
            override fun createFromParcel(parcel: Parcel): AppSettings {
                return AppSettings(parcel)
            }

            override fun newArray(size: Int): Array<AppSettings?> {
                return arrayOfNulls(size)
            }
        }
    }
}
