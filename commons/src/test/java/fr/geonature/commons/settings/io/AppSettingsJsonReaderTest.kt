package fr.geonature.commons.settings.io

import android.util.JsonReader
import fr.geonature.commons.FixtureHelper.getFixture
import fr.geonature.commons.settings.DummyAppSettings
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [AppSettingsJsonReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class AppSettingsJsonReaderTest {

    private lateinit var appSettingsJsonReader: AppSettingsJsonReader<DummyAppSettings>

    @RelaxedMockK
    private lateinit var onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<DummyAppSettings>

    @Before
    fun setUp() {
        init(this)

        every { onAppSettingsJsonJsonReaderListener.createAppSettings() } returns DummyAppSettings()

        appSettingsJsonReader = AppSettingsJsonReader(onAppSettingsJsonJsonReaderListener)
    }

    @Test
    fun testReadAppSettingsFromInvalidJsonString() {
        // when read an invalid JSON as Input
        val appSettings = appSettingsJsonReader.read("")

        // then
        assertNull(appSettings)
    }

    @Test
    fun testReadAppSettingsFromJsonString() {
        every {
            onAppSettingsJsonJsonReaderListener.readAdditionalAppSettingsData(
                any(),
                "attribute",
                any()
            )
        } answers {
            assertEquals(
                "value",
                firstArg<JsonReader>().nextString()
            )
        }

        // given app settings to read
        val json = getFixture("settings_dummy.json")

        // when read the JSON as AppSettings
        val appSettings = appSettingsJsonReader.read(json)

        // then
        verify {
            onAppSettingsJsonJsonReaderListener.readAdditionalAppSettingsData(
                any(),
                "attribute",
                any()
            )
        }

        assertNotNull(appSettings)
    }
}
