package fr.geonature.datasync.settings

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.datasync.util.parseAsDuration
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Default settings for synchronization module.
 *
 * @author S. Grimault
 */
data class DataSyncSettings(
    val geoNatureServerUrl: String,
    val taxHubServerUrl: String,
    val applicationId: Int = 0,
    val usersListId: Int = 0,
    val taxrefListId: Int = 0,
    val codeAreaType: String? = null,
    val pageSize: Int = Builder.DEFAULT_PAGE_SIZE,
    val dataSyncPeriodicity: Duration? = Builder.DEFAULT_DATA_SYNC_PERIODICITY,
    val essentialDataSyncPeriodicity: Duration? = null
) : Parcelable {

    private constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel
            .readString()
            ?.parseAsDuration(),
        parcel
            .readString()
            ?.parseAsDuration()
    )

    override fun writeToParcel(
        parcel: Parcel,
        flags: Int
    ) {
        parcel.apply {
            writeString(geoNatureServerUrl)
            writeString(taxHubServerUrl)
            writeInt(applicationId)
            writeInt(usersListId)
            writeInt(taxrefListId)
            writeString(codeAreaType)
            writeInt(pageSize)
            writeString(dataSyncPeriodicity?.toIsoString())
            writeString(essentialDataSyncPeriodicity?.toIsoString())
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    class Builder {

        /**
         * GeoNature URL.
         */
        private lateinit var geoNatureServerUrl: String

        /**
         * TaxHub URL.
         */
        private lateinit var taxHubServerUrl: String

        /**
         * GeoNature application ID in UsersHub.
         */
        private var applicationId: Int = 0

        /**
         * GeoNature selected observer list ID in UsersHub.
         */
        private var usersListId: Int = 0

        /**
         * GeoNature selected taxa list ID.
         */
        private var taxrefListId: Int = 0

        /**
         * GeoNature selected area type.
         */
        private var codeAreaType: String? = null

        /**
         * Default page size while fetching paginated values (default: 10000).
         */
        private var pageSize: Int = DEFAULT_PAGE_SIZE

        /**
         * Configure all data synchronization periodicity (default: 7 days).
         * Sets to `null` to disable it.
         */
        private var dataSyncPeriodicity: Duration? = DEFAULT_DATA_SYNC_PERIODICITY

        /**
         * Configure essential data synchronization periodicity.
         * Sets to `null` to disable it.
         */
        private var essentialDataSyncPeriodicity: Duration? = null

        /**
         * Makes a copy of given [DataSyncSettings].
         */
        fun from(dataSyncSettings: DataSyncSettings) = apply {
            geoNatureServerUrl = dataSyncSettings.geoNatureServerUrl
            taxHubServerUrl = dataSyncSettings.taxHubServerUrl
            applicationId = dataSyncSettings.applicationId
            usersListId = dataSyncSettings.usersListId
            taxrefListId = dataSyncSettings.taxrefListId
            codeAreaType = dataSyncSettings.codeAreaType
            pageSize = dataSyncSettings.pageSize
            dataSyncPeriodicity = dataSyncSettings.dataSyncPeriodicity
            essentialDataSyncPeriodicity = dataSyncSettings.essentialDataSyncPeriodicity
        }

        /**
         * Sets server base URLs. These URLs are mandatory and an [IllegalArgumentException] may be
         * thrown if [Builder.build] is called without calling [Builder.serverUrls].
         */
        fun serverUrls(
            geoNatureServerUrl: String,
            taxHubServerUrl: String
        ) = apply {
            this.geoNatureServerUrl = geoNatureServerUrl
            this.taxHubServerUrl = taxHubServerUrl
        }

        /**
         * Sets GeoNature application ID in UsersHub.
         */
        fun applicationId(applicationId: Int) = apply {
            this.applicationId = applicationId
        }

        /**
         * Sets GeoNature observer list ID in UsersHub.
         */
        fun usersListId(usersListId: Int) = apply {
            this.usersListId = usersListId
        }

        /**
         * Sets GeoNature taxa list ID.
         */
        fun taxrefListId(taxrefListId: Int) = apply {
            this.taxrefListId = taxrefListId
        }

        /**
         * Sets GeoNature area type.
         */
        fun codeAreaType(codeAreaType: String?) = apply {
            this.codeAreaType = codeAreaType
        }

        /**
         * Sets page size while fetching paginated values (default: 10000).
         */
        fun pageSize(pageSize: Int = DEFAULT_PAGE_SIZE) = apply {
            this.pageSize = pageSize
        }

        /**
         * Sets the data synchronization periodicity (default to `null`).
         *
         * The expected format describing a periodic synchronization must following the pattern
         * `DdHhMmSs` where `d`, `h`, `m`, `s` represents the time unit of the duration.
         * Each part (duration value and its time unit) of the duration is optional.
         * A time unit represents time durations at a given unit of granularity:
         *
         * - `d`: time unit representing 24 hours (i.e. one day)
         * - `h`: time unit representing 60 minutes (i.e. one hour)
         * - `m`: time unit representing 60 seconds (i.e. one minute)
         * - `s`: time unit representing one second
         *
         * Examples of valid durations:
         * - `1d12h`: 36 hours (i.e. 1.5 days)
         * - `1d`: 24 hours (i.e. one day)
         * - `4h30m`: 4.5 hours
         * - `15m`: 15 minutes
         *
         * A valid synchronization periodicity should not be less than 15 minutes: Such a
         * configuration will be ignored. If only one of these parameters is set, data
         * synchronization involves all data. If both of these parameters are set,
         * `dataSyncPeriodicity` parameter should be greater than `essentialDataSyncPeriodicity`
         * parameter.
         */
        fun dataSyncPeriodicity(
            dataSyncPeriodicity: Duration? = null,
            essentialDataSyncPeriodicity: Duration? = null
        ) = apply {
            val validDataSyncPeriodicity = dataSyncPeriodicity?.coerceAtLeast(DEFAULT_MIN_DURATION)
            val validEssentialDataSyncPeriodicity =
                essentialDataSyncPeriodicity?.coerceAtLeast(DEFAULT_MIN_DURATION)

            // no periodic synchronization is correctly configured: abort
            if (validDataSyncPeriodicity == null && validEssentialDataSyncPeriodicity == null) {
                this.dataSyncPeriodicity = null
                this.essentialDataSyncPeriodicity = null

                return@apply
            }

            // all periodic synchronizations are correctly configured
            if (validDataSyncPeriodicity != null && validEssentialDataSyncPeriodicity != null) {
                if (validEssentialDataSyncPeriodicity >= validDataSyncPeriodicity) {
                    this.dataSyncPeriodicity = validDataSyncPeriodicity
                    this.essentialDataSyncPeriodicity = null

                    return@apply
                }

                this.dataSyncPeriodicity = validDataSyncPeriodicity
                this.essentialDataSyncPeriodicity = validEssentialDataSyncPeriodicity

                return@apply
            }

            // at least one periodic synchronization is correctly configured
            arrayOf(
                validDataSyncPeriodicity,
                validEssentialDataSyncPeriodicity
            )
                .firstOrNull { it != null }
                ?.also {
                    this.dataSyncPeriodicity = it
                    this.essentialDataSyncPeriodicity = null
                }
        }

        /**
         * @see [Builder.dataSyncPeriodicity]
         */
        fun dataSyncPeriodicity(
            dataSyncPeriodicity: String? = null,
            essentialDataSyncPeriodicity: String? = null
        ) = apply {
            dataSyncPeriodicity(
                dataSyncPeriodicity?.parseAsDuration(),
                essentialDataSyncPeriodicity?.parseAsDuration()
            )
        }

        /**
         * Builds a new instance of [DataSyncSettings].
         */
        fun build(): DataSyncSettings {
            if (geoNatureServerUrl.isBlank() || taxHubServerUrl.isBlank()) {
                throw IllegalArgumentException("invalid server URLs (GeoNature URL: '$geoNatureServerUrl', TaxHub URL: '$taxHubServerUrl')")
            }

            return DataSyncSettings(
                geoNatureServerUrl,
                taxHubServerUrl,
                applicationId,
                usersListId,
                taxrefListId,
                codeAreaType,
                pageSize,
                dataSyncPeriodicity,
                essentialDataSyncPeriodicity
            )
        }

        companion object {
            const val DEFAULT_PAGE_SIZE = 10000
            val DEFAULT_MIN_DURATION = 15.toDuration(DurationUnit.MINUTES)
            val DEFAULT_DATA_SYNC_PERIODICITY = 7.toDuration(DurationUnit.DAYS)
        }
    }

    companion object CREATOR : Parcelable.Creator<DataSyncSettings> {
        override fun createFromParcel(parcel: Parcel): DataSyncSettings {
            return DataSyncSettings(parcel)
        }

        override fun newArray(size: Int): Array<DataSyncSettings?> {
            return arrayOfNulls(size)
        }
    }
}
