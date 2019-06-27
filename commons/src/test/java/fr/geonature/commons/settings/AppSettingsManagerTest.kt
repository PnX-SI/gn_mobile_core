package fr.geonature.commons.settings

import android.app.Application
import android.util.JsonReader
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.commons.FixtureHelper
import fr.geonature.commons.MockitoKotlinHelper
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import kotlinx.coroutines.runBlocking
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
import java.io.File

/**
 * Unit tests about [AppSettingsManager].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class AppSettingsManagerTest {

    private lateinit var appSettingsManager: AppSettingsManager<DummyAppSettings>

    @Mock
    private lateinit var onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<DummyAppSettings>

    @Before
    fun setUp() {
        initMocks(this)

        doReturn(DummyAppSettings()).`when`(onAppSettingsJsonJsonReaderListener)
            .createAppSettings()

        val application = getApplicationContext<Application>()
        appSettingsManager = spy(AppSettingsManager(application,
                                                    onAppSettingsJsonJsonReaderListener))
    }

    @Test
    fun testGetAppSettingsFilename() {
        // when getting the app settings filename
        val appSettingsFilename = appSettingsManager.getAppSettingsFilename()

        // then
        assertNotNull(appSettingsFilename)
        assertEquals("settings_test.json",
                     appSettingsFilename)
    }

    @Test
    fun testReadUndefinedAppSettings() {
        // when reading undefined AppSettings
        var noSuchAppSettings = runBlocking { appSettingsManager.loadAppSettings() }

        // then
        assertNull(noSuchAppSettings)

        // given non existing app settings JSON file
        doReturn(File("/mnt/sdcard",
                      "no_such_file.json")).`when`(appSettingsManager)
            .getAppSettingsAsFile()

        // when reading this file
        noSuchAppSettings = runBlocking { appSettingsManager.loadAppSettings() }

        // then
        assertNull(noSuchAppSettings)
    }

    @Test
    fun testReadAppSettings() {
        // given app settings to read
        doReturn(FixtureHelper.getFixtureAsFile("settings_dummy.json")).`when`(appSettingsManager)
            .getAppSettingsAsFile()

        `when`(onAppSettingsJsonJsonReaderListener.readAdditionalAppSettingsData(MockitoKotlinHelper.any(JsonReader::class.java),
                                                                                 MockitoKotlinHelper.eq("attribute"),
                                                                                 MockitoKotlinHelper.any(DummyAppSettings::class.java))).then {
            assertEquals("value",
                         (it.getArgument(0) as JsonReader).nextString())
        }

        // when reading this file
        val appSettings = runBlocking { appSettingsManager.loadAppSettings() }

        // then
        verify(onAppSettingsJsonJsonReaderListener,
               atMost(1)).readAdditionalAppSettingsData(MockitoKotlinHelper.any(JsonReader::class.java),
                                                        MockitoKotlinHelper.eq("attribute"),
                                                        MockitoKotlinHelper.any(DummyAppSettings::class.java))

        assertNotNull(appSettings)
    }
}