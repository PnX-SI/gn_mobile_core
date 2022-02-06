package fr.geonature.datasync.settings

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Failure
import fr.geonature.datasync.api.IGeoNatureAPIClient
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [DataSyncSettingsViewModel].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class DataSyncSettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataSyncSettingsRepository: IDataSyncSettingsRepository

    @MockK
    private lateinit var dataSyncSettingsDataSource: IDataSyncSettingsDataSource

    @RelaxedMockK
    private lateinit var geoNatureAPIClient: IGeoNatureAPIClient

    @RelaxedMockK
    private lateinit var dataSyncSettingsObserver: Observer<Either<Failure, DataSyncSettings>>

    @Before
    fun setUp() {
        init(this)

        val application = ApplicationProvider.getApplicationContext<Application>()

        dataSyncSettingsRepository = DataSyncSettingsRepositoryImpl(
            application,
            dataSyncSettingsDataSource
        )
    }

    @Test
    fun `should load DataSyncSettings`() = runTest {
        // given an existing DataSyncSettings instance from data source
        val expectedDataSyncSettings = DataSyncSettings(
            geoNatureServerUrl = "https://demo.geonature.fr/geonature",
            taxHubServerUrl = "https://demo.geonature.fr/taxhub",
            applicationId = 3,
            usersListId = 1,
            taxrefListId = 100,
            codeAreaType = "M10"
        )

        coEvery { dataSyncSettingsDataSource.load() } returns expectedDataSyncSettings

        // when
        val dataSyncSettingsViewModel = DataSyncSettingsViewModel(
            dataSyncSettingsRepository,
            geoNatureAPIClient
        )
        dataSyncSettingsViewModel
            .getDataSyncSettings()
            .observeForever(dataSyncSettingsObserver)

        // then
        verify(atLeast = 1) { dataSyncSettingsObserver.onChanged(Either.Right(expectedDataSyncSettings)) }
    }

    @Test
    fun `should update server base Urls`() {
        // given an existing DataSyncSettings instance from data source
        val expectedDataSyncSettings = DataSyncSettings(
            geoNatureServerUrl = "https://demo.geonature.fr/geonature",
            taxHubServerUrl = "https://demo.geonature.fr/taxhub",
            applicationId = 3,
            usersListId = 1,
            taxrefListId = 100,
            codeAreaType = "M10"
        )

        coEvery { dataSyncSettingsDataSource.load() } returns expectedDataSyncSettings

        // when
        val dataSyncSettingsViewModel = DataSyncSettingsViewModel(
            dataSyncSettingsRepository,
            geoNatureAPIClient
        )
        dataSyncSettingsViewModel.setServerBaseUrls(
            geoNatureServerUrl = "https://demo.geonature2.fr/geonature",
            taxHubServerUrl = "https://demo.geonature2.fr/taxhub"
        )
        dataSyncSettingsViewModel
            .getDataSyncSettings()
            .observeForever(dataSyncSettingsObserver)

        // then
        verify(atLeast = 1) {
            dataSyncSettingsObserver.onChanged(
                Either.Right(
                    DataSyncSettings
                        .Builder()
                        .from(expectedDataSyncSettings)
                        .serverUrls(
                            geoNatureServerUrl = "https://demo.geonature2.fr/geonature",
                            taxHubServerUrl = "https://demo.geonature2.fr/taxhub"
                        )
                        .build()
                )
            )
        }
        verify(atLeast = 1) {
            geoNatureAPIClient.setBaseUrls(
                geoNatureBaseUrl = "https://demo.geonature2.fr/geonature",
                taxHubBaseUrl = "https://demo.geonature2.fr/taxhub"
            )
        }
    }
}