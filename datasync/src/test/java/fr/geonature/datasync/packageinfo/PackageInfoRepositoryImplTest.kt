package fr.geonature.datasync.packageinfo

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.identity
import fr.geonature.commons.fp.orNull
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Unit tests about [IPackageInfoRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PackageInfoRepositoryTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var packageInfoRepository: IPackageInfoRepository

    @MockK
    private lateinit var availablePackageInfoDataSource: IPackageInfoDataSource

    @MockK
    private lateinit var installedPackageInfoDataSource: IPackageInfoDataSource

    @Before
    fun setUp() {
        init(this)

        val application = ApplicationProvider.getApplicationContext<Application>()

        packageInfoRepository = PackageInfoRepositoryImpl(
            application,
            availablePackageInfoDataSource,
            installedPackageInfoDataSource,
            "settings_datasync.json",
        )
    }

    @Test
    fun `should return a list of available applications`() = runTest {
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

        coEvery { availablePackageInfoDataSource.getAll() }.returns(expectedPackageInfoList)

        // when fetching available applications from data source
        val response = packageInfoRepository.getAvailableApplications()

        // then
        assertEquals(
            expectedPackageInfoList,
            response.orNull(),
        )
    }

    @Test
    fun `should return NetworkFailure if not connected while fetching a list of available applications`() =
        runTest {
            coEvery { availablePackageInfoDataSource.getAll() } throws (IOException("network failure"))

            // when fetching available applications from data source
            val response = packageInfoRepository.getAvailableApplications()

            // then
            assertTrue(response.isLeft)
            assertTrue(response.fold(::identity) {} is Failure.NetworkFailure)
        }

    @Test
    fun `should return ServerFailure if something goes wrong while fetching available applications`() =
        runTest {
            coEvery { availablePackageInfoDataSource.getAll() } throws (HttpException(
                Response.error<String>(
                    500,
                    "{}".toResponseBody("application/json".toMediaType()),
                ),
            ))

            // when fetching available applications from data source
            val response = packageInfoRepository.getAvailableApplications()

            // then
            assertTrue(response.isLeft)
            assertTrue(response.fold(::identity) {} is Failure.ServerFailure)
        }
}