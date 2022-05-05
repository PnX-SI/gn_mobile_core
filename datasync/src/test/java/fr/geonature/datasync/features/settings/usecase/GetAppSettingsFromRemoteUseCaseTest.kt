package fr.geonature.datasync.features.settings.usecase

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.identity
import fr.geonature.commons.fp.orNull
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.packageinfo.IPackageInfoRepository
import fr.geonature.datasync.packageinfo.PackageInfo
import fr.geonature.datasync.packageinfo.error.PackageInfoNotFoundFromRemoteFailure
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.settings.IDataSyncSettingsRepository
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundFailure
import io.mockk.Called
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests about [GetAppSettingsFromRemoteUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GetAppSettingsFromRemoteUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var application: Application

    private lateinit var getAppSettingsFromRemoteUseCase: GetAppSettingsFromRemoteUseCase

    @RelaxedMockK
    private lateinit var geoNatureAPIClient: IGeoNatureAPIClient

    @MockK
    private lateinit var dataSyncSettingsRepository: IDataSyncSettingsRepository

    @RelaxedMockK
    private lateinit var packageInfoRepository: IPackageInfoRepository

    @Before
    fun setUp() {
        init(this)

        application = ApplicationProvider.getApplicationContext()

        getAppSettingsFromRemoteUseCase = GetAppSettingsFromRemoteUseCase(
            application,
            geoNatureAPIClient,
            dataSyncSettingsRepository,
            packageInfoRepository,
        )
    }

    @Test
    fun `should load successfully app settings from remote`() = runTest {
        // given a list of available applications from remote data source
        val expectedPackageInfoList = listOf(
            PackageInfo(
                packageName = "fr.geonature.occtax",
                label = "Occtax",
                versionCode = 2000,
                versionName = "2.0.0",
                apkUrl = "https://demo.geonature.fr/geonature/api/static/mobile/occtax/occtax-2.0.0-generic-release.apk",
            ),
            PackageInfo(
                packageName = "fr.geonature.datasync.test",
                label = "Datasync",
                versionCode = 1000,
                versionName = "1.0.0",
                apkUrl = "https://demo.geonature.fr/geonature/api/static/mobile/datasync/datasync-1.0.0-generic-release.apk",
            ),
        )

        // and a valid data sync settings from remote
        val expectedDataSyncSettings = DataSyncSettings(
            geoNatureServerUrl = "https://demo.geonature.fr/geonature",
            taxHubServerUrl = "https://demo.geonature.fr/taxhub",
            applicationId = 3,
            usersListId = 1,
            taxrefListId = 100,
            codeAreaType = "M10",
        )

        coEvery { packageInfoRepository.getAvailableApplications() } returns Either.Right(expectedPackageInfoList)
        coEvery { dataSyncSettingsRepository.getDataSyncSettings() } returns Either.Right(expectedDataSyncSettings)

        // when loading app configuration from remote
        val response = getAppSettingsFromRemoteUseCase.run("https://demo.geonature.fr/geonature")

        // then
        assertEquals(
            expectedDataSyncSettings,
            response.orNull(),
        )
        verify { geoNatureAPIClient.setBaseUrls(IGeoNatureAPIClient.ServerUrls(geoNatureBaseUrl = "https://demo.geonature.fr/geonature")) }
        coVerify { packageInfoRepository.updateAppSettings(expectedPackageInfoList.first { it.packageName == application.packageName }) }
        verify {
            geoNatureAPIClient.setBaseUrls(IGeoNatureAPIClient.ServerUrls(
                geoNatureBaseUrl = expectedDataSyncSettings.geoNatureServerUrl,
                taxHubBaseUrl = expectedDataSyncSettings.taxHubServerUrl,
            ))
        }
    }

    @Test
    fun `should return NetworkFailure if not connected`() = runTest {
        coEvery { packageInfoRepository.getAvailableApplications() } returns Either.Left(Failure.NetworkFailure("not_connected"))

        // when loading app configuration from remote
        val response = getAppSettingsFromRemoteUseCase.run("https://demo.geonature.fr/geonature")

        assertTrue(response.isLeft)
        assertTrue(response.fold(::identity) {} is Failure.NetworkFailure)

        coVerify(exactly = 0) { packageInfoRepository.updateAppSettings(any()) }
        coVerify { dataSyncSettingsRepository.getDataSyncSettings() wasNot Called }
    }

    @Test
    fun `should return ServerFailure if something goes wrong while fetching available applications from remote`() =
        runTest {
            coEvery { packageInfoRepository.getAvailableApplications() } returns Either.Left(Failure.ServerFailure)

            // when loading app configuration from remote
            val response =
                getAppSettingsFromRemoteUseCase.run("https://demo.geonature.fr/geonature")

            assertTrue(response.isLeft)
            assertTrue(response.fold(::identity) {} is Failure.ServerFailure)

            coVerify(exactly = 0) { packageInfoRepository.updateAppSettings(any()) }
            coVerify { dataSyncSettingsRepository.getDataSyncSettings() wasNot Called }
        }

    @Test
    fun `should return PackageInfoNotFoundFromRemoteFailure if no requested application was found from remote`() =
        runTest {
            // given a list of available applications from remote data source
            val expectedPackageInfoList = listOf(
                PackageInfo(
                    packageName = "fr.geonature.occtax",
                    label = "Occtax",
                    versionCode = 2000,
                    versionName = "2.0.0",
                    apkUrl = "https://demo.geonature.fr/geonature/api/static/mobile/occtax/occtax-2.0.0-generic-release.apk",
                ),
            )

            coEvery { packageInfoRepository.getAvailableApplications() } returns Either.Right(expectedPackageInfoList)

            // when loading app configuration from remote
            val response =
                getAppSettingsFromRemoteUseCase.run("https://demo.geonature.fr/geonature")

            assertTrue(response.isLeft)
            assertEquals(PackageInfoNotFoundFromRemoteFailure(packageName = application.packageName),
                response.fold(::identity) {})

            coVerify(exactly = 0) { packageInfoRepository.updateAppSettings(any()) }
            coVerify { dataSyncSettingsRepository.getDataSyncSettings() wasNot Called }
        }

    @Test
    fun `should return DataSyncSettingsNotFoundFailure if something goes wrong while loading settings after update`() =
        runTest {
            // given a list of available applications from remote data source
            val expectedPackageInfoList = listOf(
                PackageInfo(
                    packageName = "fr.geonature.occtax",
                    label = "Occtax",
                    versionCode = 2000,
                    versionName = "2.0.0",
                    apkUrl = "https://demo.geonature.fr/geonature/api/static/mobile/occtax/occtax-2.0.0-generic-release.apk",
                ),
                PackageInfo(
                    packageName = "fr.geonature.datasync.test",
                    label = "Datasync",
                    versionCode = 1000,
                    versionName = "1.0.0",
                    apkUrl = "https://demo.geonature.fr/geonature/api/static/mobile/datasync/datasync-1.0.0-generic-release.apk",
                ),
            )

            coEvery { packageInfoRepository.getAvailableApplications() } returns Either.Right(expectedPackageInfoList)
            coEvery { dataSyncSettingsRepository.getDataSyncSettings() } returns Either.Left(DataSyncSettingsNotFoundFailure())

            // when loading app configuration from remote
            val response =
                getAppSettingsFromRemoteUseCase.run("https://demo.geonature.fr/geonature")

            assertTrue(response.isLeft)
            assertTrue(response.fold(::identity) {} is DataSyncSettingsNotFoundFailure)

            coVerify { packageInfoRepository.updateAppSettings(expectedPackageInfoList.first { it.packageName == application.packageName }) }
        }
}