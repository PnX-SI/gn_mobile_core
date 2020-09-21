package fr.geonature.sync.settings.io

import android.app.Application
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import fr.geonature.sync.FixtureHelper.getFixture
import fr.geonature.sync.settings.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests about [AppSettingsJsonReader].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class OnAppSettingsJsonReaderListenerImplTest {

    lateinit var appSettingsJsonReader: AppSettingsJsonReader<AppSettings>

    @Before
    fun setUp() {
        appSettingsJsonReader = AppSettingsJsonReader(OnAppSettingsJsonReaderListenerImpl())
    }

    @Test
    fun testReadAppSettingsFromJsonString() {
        // given a JSON settings
        val json = getFixture("settings_sync.json")

        // when read the JSON as AppSettings
        val appSettings = appSettingsJsonReader.read(json)

        // then
        assertNotNull(appSettings)
        assertEquals(
            AppSettings(
                "https://demo.geonature/geonature",
                "https://demo.geonature/taxhub",
                3,
                1,
                100,
                "M1",
                100,
                5
            ),
            appSettings
        )
    }

    @Test
    fun testReadAppSettingsFromInvalidJsonString() {
        // when read an invalid JSON as AppSettings
        val appSettings = appSettingsJsonReader.read("")

        // then
        assertNull(appSettings)
    }

    @Test
    fun testReadAppSettingsFromEmptyJsonString() {
        // when read an empty JSON as AppSettings
        val appSettings = appSettingsJsonReader.read("{}")

        // then
        assertNotNull(appSettings)
        assertEquals(
            AppSettings(),
            appSettings
        )
    }
}
