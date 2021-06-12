package fr.geonature.sync.settings

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.settings.IAppSettings

/**
 * Global internal settings.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class AppSettings(
    var geoNatureServerUrl: String? = null,
    var taxHubServerUrl: String? = null,
    var applicationId: Int = 0,
    var usersListId: Int = 0,
    var taxrefListId: Int = 0,
    var codeAreaType: String? = null,
    var pageSize: Int = DEFAULT_PAGE_SIZE,
    var essentialDataSyncPeriodicity: String? = null,
    var dataSyncPeriodicity: String? = null
) : IAppSettings {

    private constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readInt(),
        source.readInt(),
        source.readInt(),
        source.readString(),
        source.readInt(),
        source.readString(),
        source.readString()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeString(geoNatureServerUrl)
            it.writeString(taxHubServerUrl)
            it.writeInt(applicationId)
            it.writeInt(usersListId)
            it.writeInt(taxrefListId)
            it.writeString(codeAreaType)
            it.writeInt(pageSize)
            it.writeString(essentialDataSyncPeriodicity)
            it.writeString(dataSyncPeriodicity)
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
