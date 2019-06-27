package fr.geonature.commons.settings.io

import android.util.JsonReader
import fr.geonature.commons.FixtureHelper.getFixture
import fr.geonature.commons.MockitoKotlinHelper
import fr.geonature.commons.settings.DummyAppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.atMost
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [AppSettingsJsonReader].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class AppSettingsJsonReaderTest {

    private lateinit var appSettingsJsonReader: AppSettingsJsonReader<DummyAppSettings>

    @Mock
    private lateinit var onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<DummyAppSettings>

    @Before
    fun setUp() {
        initMocks(this)

        doReturn(DummyAppSettings()).`when`(onAppSettingsJsonJsonReaderListener)
            .createAppSettings()

        appSettingsJsonReader = spy(AppSettingsJsonReader(onAppSettingsJsonJsonReaderListener))
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
        `when`(onAppSettingsJsonJsonReaderListener.readAdditionalAppSettingsData(MockitoKotlinHelper.any(JsonReader::class.java),
                                                                                 MockitoKotlinHelper.eq("attribute"),
                                                                                 MockitoKotlinHelper.any(DummyAppSettings::class.java))).then {
            assertEquals("value",
                         (it.getArgument(0) as JsonReader).nextString())
        }

        // given app settings to read
        val json = getFixture("settings_dummy.json")

        // when read the JSON as AppSettings
        val appSettings = appSettingsJsonReader.read(json)

        // then
        verify(onAppSettingsJsonJsonReaderListener,
               atMost(1)).readAdditionalAppSettingsData(MockitoKotlinHelper.any(JsonReader::class.java),
                                                        MockitoKotlinHelper.eq("attribute"),
                                                        MockitoKotlinHelper.any(DummyAppSettings::class.java))

        assertNotNull(appSettings)
    }
}