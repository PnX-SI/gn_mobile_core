package fr.geonature.commons.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.observeOnce
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [AppSettingsViewModel].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class AppSettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var appSettingsManager: IAppSettingsManager<DummyAppSettings>

    private lateinit var appSettingsViewModel: DummyAppSettingsViewModel

    @Before
    fun setUp() {
        init(this)
        appSettingsViewModel = spyk(DummyAppSettingsViewModel(appSettingsManager))
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should get app settings filename from Manager`() {
        // when getting the app settings filename
        every { appSettingsManager.getAppSettingsFilename() } returns "settings_test.json"

        val appSettingsFilename = appSettingsViewModel.getAppSettingsFilename()

        // then
        assertNotNull(appSettingsFilename)
        assertEquals(
            "settings_test.json",
            appSettingsFilename
        )
    }

    @Test
    fun `should read app settings from Manager`() {
        val expectedAppSettings = DummyAppSettings(attribute = "value")
        coEvery { appSettingsManager.loadAppSettings() } returns expectedAppSettings

        appSettingsViewModel
            .loadAppSettings()
            .observeOnce {
                assertEquals(
                    expectedAppSettings,
                    it
                )
            }
    }

    class DummyAppSettingsViewModel(appSettingsManager: IAppSettingsManager<DummyAppSettings>) :
        AppSettingsViewModel<DummyAppSettings>(appSettingsManager)
}
