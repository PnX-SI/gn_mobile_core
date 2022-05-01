package fr.geonature.datasync.features.settings.usecase

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.identity
import fr.geonature.commons.fp.orNull
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.packageinfo.IPackageInfoRepository
import fr.geonature.datasync.packageinfo.PackageInfo
import fr.geonature.datasync.packageinfo.error.PackageInfoNotFoundFailure
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.settings.IDataSyncSettingsRepository
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundFailure
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
 * Unit tests about [UpdateSettingsFromRemoteUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class UpdateSettingsFromRemoteUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var application: Application

    private lateinit var updateSettingsFromRemoteUseCase: UpdateSettingsFromRemoteUseCase

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

        updateSettingsFromRemoteUseCase = UpdateSettingsFromRemoteUseCase(
            application,
            geoNatureAPIClient,
            dataSyncSettingsRepository,
            packageInfoRepository,
        )
    }

    @Test
    fun `should update existing app settings from remote`() = runTest {
        // given a list of available applications
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
            ).apply {
                settings = mapOf<String, Any>(
                    "sync" to mapOf<String, Any>(
                        "geoNatureServerUrl" to "https://demo.geonature.fr/geonature2",
                        "taxHubServerUrl" to "https://demo.geonature.fr/taxhub2",
                    ),
                )
            },
        )

        // and a valid existing data sync settings
        val expectedDataSyncSettings = DataSyncSettings(
            geoNatureServerUrl = "https://demo.geonature.fr/geonature",
            taxHubServerUrl = "https://demo.geonature.fr/taxhub",
            applicationId = 3,
            usersListId = 1,
            taxrefListId = 100,
            codeAreaType = "M10",
        )
        val expectedDataSyncSettingsAfterUpdate = DataSyncSettings(
            geoNatureServerUrl = "https://demo.geonature.fr/geonature2",
            taxHubServerUrl = "https://demo.geonature.fr/taxhub2",
            applicationId = 3,
            usersListId = 1,
            taxrefListId = 100,
            codeAreaType = "M10",
        )

        coEvery { dataSyncSettingsRepository.getDataSyncSettings() } returns Either.Right(expectedDataSyncSettings) andThen Either.Right(expectedDataSyncSettingsAfterUpdate)
        coEvery { packageInfoRepository.getAllApplications() } returns flow { emit(expectedPackageInfoList) }

        // when updating existing app settings
        val response = updateSettingsFromRemoteUseCase.run(BaseUseCase.None())

        // then
        assertEquals(
            expectedPackageInfoList.first { it.packageName == application.packageName },
            response.orNull(),
        )
        verify {
            geoNatureAPIClient.setBaseUrls(IGeoNatureAPIClient.ServerUrls(
                geoNatureBaseUrl = expectedDataSyncSettings.geoNatureServerUrl,
                taxHubBaseUrl = expectedDataSyncSettings.taxHubServerUrl,
            ))
        }
        coVerify { packageInfoRepository.updateAppSettings(expectedPackageInfoList.first { it.packageName == application.packageName }) }
        verify {
            geoNatureAPIClient.setBaseUrls(IGeoNatureAPIClient.ServerUrls(
                geoNatureBaseUrl = expectedDataSyncSettingsAfterUpdate.geoNatureServerUrl,
                taxHubBaseUrl = expectedDataSyncSettingsAfterUpdate.taxHubServerUrl,
            ))
        }
    }

    @Test
    fun `should return DataSyncSettingsNotFoundFailure if no settings was found locally`() =
        runTest {
            coEvery { dataSyncSettingsRepository.getDataSyncSettings() } returns Either.Left(DataSyncSettingsNotFoundFailure())

            // when updating existing app settings
            val response = updateSettingsFromRemoteUseCase.run(BaseUseCase.None())

            assertTrue(response.isLeft)
            assertEquals(DataSyncSettingsNotFoundFailure(),
                response.fold(::identity) {})
            coVerify(exactly = 0) { packageInfoRepository.updateAppSettings(any()) }
        }

    @Test
    fun `should return PackageInfoNotFoundFailure if no requested application was found from remote`() =
        runTest {
            // given a list of available applications
            val expectedPackageInfoList = listOf(
                PackageInfo(
                    packageName = "fr.geonature.occtax",
                    label = "Occtax",
                    versionCode = 2000,
                    versionName = "2.0.0",
                    apkUrl = "https://demo.geonature.fr/geonature/api/static/mobile/occtax/occtax-2.0.0-generic-release.apk",
                ),
            )

            // and a valid existing data sync settings
            val expectedDataSyncSettings = DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10",
            )

            coEvery { dataSyncSettingsRepository.getDataSyncSettings() } returns Either.Right(expectedDataSyncSettings)
            coEvery { packageInfoRepository.getAllApplications() } returns flow { emit(expectedPackageInfoList) }

            // when updating existing app settings
            val response = updateSettingsFromRemoteUseCase.run(BaseUseCase.None())

            assertTrue(response.isLeft)
            assertEquals(PackageInfoNotFoundFailure(packageName = application.packageName),
                response.fold(::identity) {})
            coVerify(exactly = 0) { packageInfoRepository.updateAppSettings(any()) }
        }
}