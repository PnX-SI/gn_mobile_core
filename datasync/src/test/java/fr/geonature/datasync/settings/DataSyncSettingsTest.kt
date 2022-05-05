package fr.geonature.datasync.settings

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Unit tests about [DataSyncSettings].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DataSyncSettingsTest {

    @Test
    fun `should build DataSyncSettings from Builder`() {
        assertEquals(
            DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10"
            ),
            DataSyncSettings
                .Builder()
                .serverUrls(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                )
                .applicationId(3)
                .usersListId(1)
                .taxrefListId(100)
                .codeAreaType("M10")
                .build()
        )
    }

    @Test
    fun `should copy DataSyncSettings instance from Builder`() {
        // given a DataSyncSettings instance to copy
        val expectedDataSyncSettings = DataSyncSettings(
            geoNatureServerUrl = "https://demo.geonature.fr/geonature",
            taxHubServerUrl = "https://demo.geonature.fr/taxhub",
            applicationId = 3,
            usersListId = 1,
            taxrefListId = 100,
            codeAreaType = "M10",
            pageSize = DataSyncSettings.Builder.DEFAULT_PAGE_SIZE,
            dataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES),
            essentialDataSyncPeriodicity = 15.toDuration(DurationUnit.MINUTES)
        )

        // when create a copy of this instance from Builder
        val dataSyncSettingsCopied = DataSyncSettings
            .Builder()
            .from(expectedDataSyncSettings)
            .build()

        // then
        assertEquals(
            expectedDataSyncSettings,
            dataSyncSettingsCopied
        )
    }

    @Test
    fun `should throw IllegalArgumentException if server URLs are not set correctly`() {
        assertEquals(
            "invalid server URLs (GeoNature URL: 'https://demo.geonature.fr/geonature', TaxHub URL: '')",
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                DataSyncSettings
                    .Builder()
                    .serverUrls(
                        geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                        taxHubServerUrl = ""
                    )
                    .build()
            }.message
        )

        assertEquals(
            "invalid server URLs (GeoNature URL: '', TaxHub URL: 'https://demo.geonature.fr/taxhub')",
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                DataSyncSettings
                    .Builder()
                    .serverUrls(
                        geoNatureServerUrl = "",
                        taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                    )
                    .build()
            }.message
        )

        assertEquals(
            "invalid server URLs (GeoNature URL: '', TaxHub URL: '')",
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                DataSyncSettings
                    .Builder()
                    .serverUrls(
                        geoNatureServerUrl = "",
                        taxHubServerUrl = ""
                    )
                    .build()
            }.message
        )
    }

    @Test
    fun `should build DataSyncSettings with valid data sync periodicity from Builder`() {
        // when both data synchronization periodicity are given
        assertEquals(
            DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10",
                pageSize = DataSyncSettings.Builder.DEFAULT_PAGE_SIZE,
                dataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES),
                essentialDataSyncPeriodicity = 15.toDuration(DurationUnit.MINUTES)
            ),
            DataSyncSettings
                .Builder()
                .serverUrls(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                )
                .applicationId(3)
                .usersListId(1)
                .taxrefListId(100)
                .codeAreaType("M10")
                .dataSyncPeriodicity(
                    dataSyncPeriodicity = "20m",
                    essentialDataSyncPeriodicity = "15m"
                )
                .build()
        )

        // when only data synchronization periodicity is given
        assertEquals(
            DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10",
                pageSize = DataSyncSettings.Builder.DEFAULT_PAGE_SIZE,
                dataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES),
            ),
            DataSyncSettings
                .Builder()
                .serverUrls(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                )
                .applicationId(3)
                .usersListId(1)
                .taxrefListId(100)
                .codeAreaType("M10")
                .dataSyncPeriodicity(dataSyncPeriodicity = "20m")
                .build()
        )

        // when only essential data synchronization periodicity is given
        assertEquals(
            DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10",
                pageSize = DataSyncSettings.Builder.DEFAULT_PAGE_SIZE,
                dataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES),
            ),
            DataSyncSettings
                .Builder()
                .serverUrls(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                )
                .applicationId(3)
                .usersListId(1)
                .taxrefListId(100)
                .codeAreaType("M10")
                .dataSyncPeriodicity(essentialDataSyncPeriodicity = "20m")
                .build()
        )

        // when data synchronization periodicity is too short
        assertEquals(
            DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10",
                pageSize = DataSyncSettings.Builder.DEFAULT_PAGE_SIZE,
                dataSyncPeriodicity = 15.toDuration(DurationUnit.MINUTES)
            ),
            DataSyncSettings
                .Builder()
                .serverUrls(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                )
                .applicationId(3)
                .usersListId(1)
                .taxrefListId(100)
                .codeAreaType("M10")
                .dataSyncPeriodicity(dataSyncPeriodicity = 14.toDuration(DurationUnit.MINUTES))
                .build()
        )

        // when both data synchronization periodicity are too short
        assertEquals(
            DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10",
                pageSize = DataSyncSettings.Builder.DEFAULT_PAGE_SIZE,
                dataSyncPeriodicity = 15.toDuration(DurationUnit.MINUTES)
            ),
            DataSyncSettings
                .Builder()
                .serverUrls(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                )
                .applicationId(3)
                .usersListId(1)
                .taxrefListId(100)
                .codeAreaType("M10")
                .dataSyncPeriodicity(
                    dataSyncPeriodicity = 14.toDuration(DurationUnit.MINUTES),
                    essentialDataSyncPeriodicity = 10.toDuration(DurationUnit.MINUTES)
                )
                .build()
        )

        // when essential data synchronization periodicity is greater than data synchronization periodicity
        assertEquals(
            DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10",
                pageSize = DataSyncSettings.Builder.DEFAULT_PAGE_SIZE,
                dataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
            ),
            DataSyncSettings
                .Builder()
                .serverUrls(
                    geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                    taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                )
                .applicationId(3)
                .usersListId(1)
                .taxrefListId(100)
                .codeAreaType("M10")
                .dataSyncPeriodicity(
                    dataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES),
                    essentialDataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES)
                )
                .build()
        )
    }

    @Test
    fun `should create DataSyncSettings from Parcelable`() {
        // given an DataSyncSettings instance
        val dataASyncSettings = DataSyncSettings(
            geoNatureServerUrl = "https://demo.geonature.fr/geonature",
            taxHubServerUrl = "https://demo.geonature.fr/taxhub",
            applicationId = 3,
            usersListId = 1,
            taxrefListId = 100,
            codeAreaType = "M10",
            pageSize = DataSyncSettings.Builder.DEFAULT_PAGE_SIZE,
            dataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES),
            essentialDataSyncPeriodicity = 15.toDuration(DurationUnit.MINUTES)
        )

        // when we obtain a Parcel object to write the DataSyncSettings instance to it
        val parcel = Parcel.obtain()
        dataASyncSettings.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            dataASyncSettings,
            DataSyncSettings.CREATOR.createFromParcel(parcel)
        )
    }
}