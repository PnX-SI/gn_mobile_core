package fr.geonature.commons.settings

import android.app.Application
import android.util.JsonReader
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.FixtureHelper.getFixtureAsFile
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Unit tests about [AppSettingsManagerImpl].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AppSettingsManagerTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var appSettingsManager: AppSettingsManagerImpl<DummyAppSettings>
    private lateinit var onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<DummyAppSettings>

    @Before
    fun setUp() {
        onAppSettingsJsonJsonReaderListener = spyk(object :
            AppSettingsJsonReader.OnAppSettingsJsonReaderListener<DummyAppSettings> {
            override fun createAppSettings(): DummyAppSettings {
                return DummyAppSettings()
            }

            override fun readAdditionalAppSettingsData(
                reader: JsonReader,
                keyName: String,
                appSettings: DummyAppSettings
            ) {
                when (keyName) {
                    "attribute" -> appSettings.attribute = reader.nextString()
                    else -> reader.skipValue()
                }
            }
        })

        val application = getApplicationContext<Application>()

        appSettingsManager = spyk(
            AppSettingsManagerImpl(
                application,
                onAppSettingsJsonJsonReaderListener,
                coroutineTestRule.testDispatcher
            )
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should get app settings filename from current context`() {
        // when getting the app settings filename
        val appSettingsFilename = appSettingsManager.getAppSettingsFilename()

        // then
        assertNotNull(appSettingsFilename)
        assertEquals(
            "settings_test.json",
            appSettingsFilename
        )
    }

    @Test
    fun `should return undefined app settings`() =
        runTest {
            // when reading undefined AppSettings
            var noSuchAppSettings = appSettingsManager.loadAppSettings()

            // then
            assertNull(noSuchAppSettings)

            // given non existing app settings JSON file
            every {
                appSettingsManager.getAppSettingsAsFile()
            } returns File(
                "/mnt/sdcard",
                "no_such_file.json"
            )

            // when reading this file
            noSuchAppSettings = appSettingsManager.loadAppSettings()

            // then
            assertNull(noSuchAppSettings)
        }

    @Test
    fun `should read app settings from JSON file`() =
        runTest {
            // given app settings to read
            every {
                appSettingsManager.getAppSettingsAsFile()
            } returns runCatching { getFixtureAsFile("settings_dummy.json") }.getOrThrow()

            // when reading this file
            val appSettings = appSettingsManager.loadAppSettings()

            // then
            verify { onAppSettingsJsonJsonReaderListener.createAppSettings() }
            verify {
                onAppSettingsJsonJsonReaderListener.readAdditionalAppSettingsData(
                    any(),
                    "attribute",
                    any()
                )
            }

            confirmVerified(onAppSettingsJsonJsonReaderListener)

            assertNotNull(appSettings)
            assertEquals(
                DummyAppSettings("value"),
                appSettings
            )
        }
}
