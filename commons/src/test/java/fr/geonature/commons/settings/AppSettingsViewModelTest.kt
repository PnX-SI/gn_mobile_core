package fr.geonature.commons.settings

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.settings.io.AppSettingsJsonReader
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.MockitoAnnotations.initMocks
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

    @Mock
    private lateinit var onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<DummyAppSettings>

    @Mock
    private lateinit var observer: Observer<DummyAppSettings>

    private lateinit var application: Application
    private lateinit var appSettingsViewModel: DummyAppSettingsViewModel
    private lateinit var appSettingsManager: AppSettingsManager<DummyAppSettings>

    @Before
    fun setUp() {
        initMocks(this)

        application = spy(ApplicationProvider.getApplicationContext<Application>())
        doReturn("fr.geonature.commons").`when`(application)
            .packageName

        appSettingsViewModel = spy(DummyAppSettingsViewModel(application,
                                                             onAppSettingsJsonJsonReaderListener))
        appSettingsManager = spy(appSettingsViewModel.appSettingsManager)
        appSettingsManager.appSettings.observeForever(observer)
    }

    @Test
    fun testCreateFromFactory() {
        // given Factory
        val factory = AppSettingsViewModel.Factory {
            DummyAppSettingsViewModel(application,
                                      onAppSettingsJsonJsonReaderListener)
        }

        // when create AppSettingsViewModel instance from this factory
        val appSettingsViewModelFromFactory = factory.create(DummyAppSettingsViewModel::class.java)

        // then
        assertNotNull(appSettingsViewModelFromFactory)
    }

    class DummyAppSettingsViewModel(application: Application,
                                    onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<DummyAppSettings>) : AppSettingsViewModel<DummyAppSettings>(application,
                                                                                                                                                                                           onAppSettingsJsonJsonReaderListener)
}