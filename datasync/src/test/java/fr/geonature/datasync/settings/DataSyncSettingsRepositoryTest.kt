package fr.geonature.datasync.settings

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.Either.Right
import fr.geonature.commons.fp.identity
import fr.geonature.commons.fp.orNull
import fr.geonature.datasync.api.GeoNatureMissingConfigurationFailure
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundException
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundFailure
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests about [IDataSyncSettingsRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DataSyncSettingsRepositoryTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var dataSyncSettingsRepository: IDataSyncSettingsRepository

    @MockK
    private lateinit var dataSyncSettingsDataSource: IDataSyncSettingsDataSource

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
        dataSyncSettingsRepository.dataSyncSettings.observeForever(dataSyncSettingsObserver)
    }

    @Test
    fun `should return undefined DataSyncSettingsNotFoundFailure if none was loaded from data source`() =
        runTest {
            // given non existing DataSyncSettings instance from data source
            coEvery { dataSyncSettingsDataSource.load() }.throws(DataSyncSettingsNotFoundException())

            // when
            val dataSyncSettingsResponse = dataSyncSettingsRepository.getDataSyncSettings()

            // then
            assertTrue(dataSyncSettingsResponse.isLeft)
            val failure = dataSyncSettingsResponse.fold({ it },
                { fail("shouldn't be executed") })
            assertTrue(failure is DataSyncSettingsNotFoundFailure)
        }

    @Test
    fun `should return DataSyncSettingsNotFoundFailure with existing geoNature server URL if none was loaded from data source`() =
        runTest {
            // and server base URL from preferences
            dataSyncSettingsRepository.setServerBaseUrl("https://demo.geonature.fr/geonature")

            // given non existing DataSyncSettings instance from data source
            coEvery { dataSyncSettingsDataSource.load() }.throws(DataSyncSettingsNotFoundException(source = "no_such_settings.json"))

            // when
            val dataSyncSettingsResponse = dataSyncSettingsRepository.getDataSyncSettings()

            // then
            assertTrue(dataSyncSettingsResponse.isLeft)
            assertEquals(
                DataSyncSettingsNotFoundFailure(
                    source = "no_such_settings.json",
                    geoNatureBaseUrl = "https://demo.geonature.fr/geonature",
                ),
                dataSyncSettingsResponse.fold(::identity) {},
            )
        }

    @Test
    fun `should return loaded DataSyncSettings from data source`() =
        runTest {
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
            val dataSyncSettingsResponse = dataSyncSettingsRepository.getDataSyncSettings()

            // then
            assertEquals(
                expectedDataSyncSettings,
                dataSyncSettingsResponse.orNull()
            )
            verify(atLeast = 1) { dataSyncSettingsObserver.onChanged(Right(expectedDataSyncSettings)) }
        }

    @Test
    fun `should return updated DataSyncSettings from preferences`() =
        runTest {
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

            // and server base URL from preferences
            dataSyncSettingsRepository.setServerBaseUrl("https://demo.geonature.fr/geonature2")

            // when
            val dataSyncSettingsResponse = dataSyncSettingsRepository.getDataSyncSettings()

            // then
            assertEquals(
                DataSyncSettings
                    .Builder()
                    .from(expectedDataSyncSettings)
                    .serverUrls(
                        geoNatureServerUrl = "https://demo.geonature.fr/geonature2",
                        taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                    )
                    .build(),
                dataSyncSettingsResponse.orNull()
            )
            verify(atLeast = 1) {
                dataSyncSettingsObserver.onChanged(
                    Right(
                        DataSyncSettings
                            .Builder()
                            .from(expectedDataSyncSettings)
                            .serverUrls(
                                geoNatureServerUrl = "https://demo.geonature.fr/geonature2",
                                taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                            )
                            .build()
                    )
                )
            }
        }

    @Test
    fun `should update server base URLs to existing loaded DataSyncSettings`() =
        runTest {
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

            dataSyncSettingsRepository.getDataSyncSettings()

            // when updating server base URLs
            dataSyncSettingsRepository.setServerBaseUrl("https://demo.geonature.fr/geonature2")

            // then
            verify(atLeast = 1) {
                dataSyncSettingsObserver.onChanged(
                    Right(
                        DataSyncSettings
                            .Builder()
                            .from(expectedDataSyncSettings)
                            .serverUrls(
                                geoNatureServerUrl = "https://demo.geonature.fr/geonature2",
                                taxHubServerUrl = "https://demo.geonature.fr/taxhub"
                            )
                            .build()
                    )
                )
            }
        }

    @Test
    fun `should return GeoNatureMissingConfigurationFailure if no server URLs was configured`() =
        runTest {
            // when getting current server base URLs
            val serverBaseUrlsResponse = dataSyncSettingsRepository.getServerBaseUrls()

            // then
            assertTrue(serverBaseUrlsResponse.isLeft)
            assertEquals(
                GeoNatureMissingConfigurationFailure,
                serverBaseUrlsResponse.fold(::identity) {},
            )
        }
}