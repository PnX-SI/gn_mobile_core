package fr.geonature.datasync.settings

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.datasync.util.parseAsDuration
import org.tinylog.Logger
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private fun readAuthModeFromParcel(parcel: Parcel): DataSyncSettings.AuthMode {
    return runCatching {
        DataSyncSettings.AuthMode.valueOf(parcel.readString().orEmpty().uppercase())
    }.getOrElse { DataSyncSettings.AuthMode.GEONATURE }
}

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
    val taxrefListId: Int = -1,
    val codeAreaType: String? = null,
    val pageSize: Int = Builder.DEFAULT_PAGE_SIZE,
    val dataSyncPeriodicity: Duration? = Builder.DEFAULT_DATA_SYNC_PERIODICITY,
    val essentialDataSyncPeriodicity: Duration? = null,
    val authMode: AuthMode = AuthMode.GEONATURE,
    val keycloakProviderId: String? = null,
    val keycloakLoginPath: String? = null,
    val keycloakAuthorizePath: String? = null,
    val keycloakCurrentUserPath: String? = null,
    val keycloakRedirectUri: String? = null,
    val keycloakMobileLoginPath: String? = null
) : Parcelable {

    enum class AuthMode {
        GEONATURE,
        KEYCLOAK
    }

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
            ?.parseAsDuration(),
        readAuthModeFromParcel(parcel),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
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
            writeString(authMode.name.lowercase())
            writeString(keycloakProviderId)
            writeString(keycloakLoginPath)
            writeString(keycloakAuthorizePath)
            writeString(keycloakCurrentUserPath)
            writeString(keycloakRedirectUri)
            writeString(keycloakMobileLoginPath)
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
        private var taxrefListId: Int = -1

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
         * Authentication mode (default: GeoNature login/password flow).
         */
        private var authMode: AuthMode = AuthMode.GEONATURE

        /**
         * Keycloak provider id (e.g. "keycloak").
         */
        private var keycloakProviderId: String? = null

        /**
         * Keycloak provider login path (e.g. "api/auth/login/{provider_id}").
         */
        private var keycloakLoginPath: String? = null

        /**
         * Keycloak provider authorize callback path (e.g. "api/auth/authorize/{provider_id}").
         */
        private var keycloakAuthorizePath: String? = null

        /**
         * Keycloak current user path (e.g. "api/auth/get_current_user").
         */
        private var keycloakCurrentUserPath: String? = null

        /**
         * Mobile redirect URI registered in Keycloak.
         */
        private var keycloakRedirectUri: String? = null

        /**
         * GeoNature mobile endpoint for keycloak code exchange.
         */
        private var keycloakMobileLoginPath: String? = null

        /**
         * Makes a copy of given [DataSyncSettings].
         */
        fun from(dataSyncSettings: DataSyncSettings?) =
            apply {
                if (dataSyncSettings == null) return@apply

                geoNatureServerUrl = dataSyncSettings.geoNatureServerUrl
                taxHubServerUrl = dataSyncSettings.taxHubServerUrl
                applicationId = dataSyncSettings.applicationId
                usersListId = dataSyncSettings.usersListId
                taxrefListId = dataSyncSettings.taxrefListId
                codeAreaType = dataSyncSettings.codeAreaType
                pageSize = dataSyncSettings.pageSize
                dataSyncPeriodicity = dataSyncSettings.dataSyncPeriodicity
                essentialDataSyncPeriodicity = dataSyncSettings.essentialDataSyncPeriodicity
                authMode = dataSyncSettings.authMode
                keycloakProviderId = dataSyncSettings.keycloakProviderId
                keycloakLoginPath = dataSyncSettings.keycloakLoginPath
                keycloakAuthorizePath = dataSyncSettings.keycloakAuthorizePath
                keycloakCurrentUserPath = dataSyncSettings.keycloakCurrentUserPath
                keycloakRedirectUri = dataSyncSettings.keycloakRedirectUri
                keycloakMobileLoginPath = dataSyncSettings.keycloakMobileLoginPath
            }

        /**
         * Sets server base URLs. These URLs are mandatory and an [IllegalArgumentException] may be
         * thrown if [Builder.build] is called without calling [Builder.serverUrls].
         */
        fun serverUrls(
            geoNatureServerUrl: String,
            taxHubServerUrl: String
        ) =
            apply {
                this.geoNatureServerUrl = geoNatureServerUrl
                this.taxHubServerUrl = taxHubServerUrl
            }

        /**
         * Sets GeoNature application ID in UsersHub.
         */
        fun applicationId(applicationId: Int) =
            apply {
                this.applicationId = applicationId
            }

        /**
         * Sets GeoNature observer list ID in UsersHub.
         */
        fun usersListId(usersListId: Int) =
            apply {
                this.usersListId = usersListId
            }

        /**
         * Sets GeoNature taxa list ID.
         */
        fun taxrefListId(taxrefListId: Int) =
            apply {
                this.taxrefListId = taxrefListId
            }

        /**
         * Sets GeoNature area type.
         */
        fun codeAreaType(codeAreaType: String?) =
            apply {
                this.codeAreaType = codeAreaType
            }

        /**
         * Sets page size while fetching paginated values (default: 10000).
         */
        fun pageSize(pageSize: Int = DEFAULT_PAGE_SIZE) =
            apply {
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
        ) =
            apply {
                val validDataSyncPeriodicity = dataSyncPeriodicity?.takeIf {
                    if (it < DEFAULT_MIN_DURATION) Logger.warn { "data synchronization periodicity cannot be shorter than $DEFAULT_MIN_DURATION, use default" }
                    it >= DEFAULT_MIN_DURATION
                }
                val validEssentialDataSyncPeriodicity = essentialDataSyncPeriodicity
                    ?.takeIf {
                        if (it < DEFAULT_MIN_DURATION) Logger.warn { "essential data synchronization periodicity cannot be shorter than $DEFAULT_MIN_DURATION" }
                        it >= DEFAULT_MIN_DURATION
                    }
                    ?.takeIf {
                        if (validDataSyncPeriodicity != null && it >= validDataSyncPeriodicity) Logger.warn { "essential data synchronization periodicity cannot be greater than data synchronization periodicity" }
                        if (validDataSyncPeriodicity == null) true else it < validDataSyncPeriodicity
                    }

                // no periodic synchronization is correctly configured: use default
                if (validDataSyncPeriodicity == null && validEssentialDataSyncPeriodicity == null) {
                    this.dataSyncPeriodicity =
                        if (dataSyncPeriodicity == null) null else DEFAULT_DATA_SYNC_PERIODICITY
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
        ) =
            apply {
                dataSyncPeriodicity(
                    dataSyncPeriodicity?.parseAsDuration(),
                    essentialDataSyncPeriodicity?.parseAsDuration()
                )
            }

        /**
         * Sets authentication mode.
         */
        fun authMode(authMode: String? = null) =
            apply {
                this.authMode = when (authMode?.trim()?.lowercase()) {
                    "keycloak" -> AuthMode.KEYCLOAK
                    else -> AuthMode.GEONATURE
                }
            }

        /**
         * Sets Keycloak settings.
         */
        fun keycloak(
            providerId: String? = null,
            loginPath: String? = null,
            authorizePath: String? = null,
            currentUserPath: String? = null,
            redirectUri: String? = null,
            mobileLoginPath: String? = null
        ) =
            apply {
                keycloakProviderId = providerId?.takeIf { it.isNotBlank() }
                keycloakLoginPath = loginPath?.takeIf { it.isNotBlank() }
                keycloakAuthorizePath = authorizePath?.takeIf { it.isNotBlank() }
                keycloakCurrentUserPath = currentUserPath?.takeIf { it.isNotBlank() }
                keycloakRedirectUri = redirectUri?.takeIf { it.isNotBlank() }
                keycloakMobileLoginPath = mobileLoginPath?.takeIf { it.isNotBlank() }
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
                essentialDataSyncPeriodicity,
                authMode,
                keycloakProviderId,
                keycloakLoginPath,
                keycloakAuthorizePath,
                keycloakCurrentUserPath,
                keycloakRedirectUri,
                keycloakMobileLoginPath
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
