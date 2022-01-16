package fr.geonature.sync.settings

import android.app.Application
import android.os.Parcel
import fr.geonature.datasync.settings.DataSyncSettings
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Unit tests about [AppSettings].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class AppSettingsTest {
    @Test
    fun testParcelable() {
        // given an AppSettings instance
        val appSettings = AppSettings(
            sync = DataSyncSettings
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
                    dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                    essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
                )
                .build()
        )

        // when we obtain a Parcel object to write the AppSettings instance to it
        val parcel = Parcel.obtain()
        appSettings.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            appSettings,
            AppSettings.CREATOR.createFromParcel(parcel)
        )
    }
}
