package fr.geonature.sync.settings

import android.app.Application
import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
            "https://demo.geonature/geonature",
            "https://demo.geonature/taxhub",
            3,
            1,
            100
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
