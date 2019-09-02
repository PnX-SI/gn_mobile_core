package fr.geonature.commons.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.MainApplication
import fr.geonature.commons.observeOnce
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.MockitoAnnotations.initMocks
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [AbstractAppSettingsViewModel].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class AppSettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var appSettingsManager: AppSettingsManager<DummyAppSettings>

    private lateinit var appSettingsViewModel: AppSettingsViewModel
    private lateinit var application: MainApplication

    @Before
    fun setUp() {
        initMocks(this)

        application = spy(MainApplication())
        doReturn("fr.geonature.commons").`when`(application)
            .packageName

        appSettingsViewModel = spy(AppSettingsViewModel(application))
        doReturn(appSettingsManager).`when`(appSettingsViewModel)
            .appSettingsManager
    }

    @Test
    fun testLoadAppSettingsFromApplication() {
        val appSettings = DummyAppSettings("attribute")

        runBlocking {
            doReturn(appSettings).`when`(application)
                .getAppSettings<DummyAppSettings>()

            appSettingsViewModel.getAppSettings()
                .observeOnce {
                    assertEquals(appSettings,
                                 it)
                }
        }
    }

    @Test
    fun testLoadAppSettingsFromAppSettingsManager() {
        val appSettings = DummyAppSettings("attribute")

        runBlocking {
            doReturn(appSettings).`when`(appSettingsManager)
                .loadAppSettings()

            appSettingsViewModel.getAppSettings()
                .observeOnce {
                    assertEquals(appSettings,
                                 it)
                    assertEquals(appSettings,
                                 application.getAppSettings())
                }
        }
    }

    class AppSettingsViewModel(application: MainApplication) : AbstractAppSettingsViewModel<DummyAppSettings>(application) {

        override fun getOnAppSettingsJsonReaderListener(): AppSettingsJsonReader.OnAppSettingsJsonReaderListener<DummyAppSettings> {
            return Mockito.mock(AppSettingsJsonReader.OnAppSettingsJsonReaderListener::class.java) as AppSettingsJsonReader.OnAppSettingsJsonReaderListener<DummyAppSettings>
        }
    }
}