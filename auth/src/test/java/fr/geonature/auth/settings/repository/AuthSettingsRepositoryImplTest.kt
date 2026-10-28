package fr.geonature.auth.settings.repository

import fr.geonature.auth.settings.data.IAuthSettingsDataSource
import fr.geonature.auth.settings.domain.AuthSettings
import fr.geonature.auth.settings.error.AuthSettingsException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests about [AuthSettingsRepositoryImpl].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class AuthSettingsRepositoryImplTest {

    private val dataSource = mockk<IAuthSettingsDataSource>()
    private val repository = AuthSettingsRepositoryImpl(dataSource)

    @Test
    fun `should return success with loaded settings`() = runTest {
        coEvery { dataSource.load() } returns AuthSettings(applicationId = 3)

        val result = repository.getAuthSettings()

        assertTrue(result.isSuccess)
        assertEquals(
            AuthSettings(applicationId = 3),
            result.getOrNull()
        )
    }

    @Test
    fun `should return failure if settings cannot be loaded`() = runTest {
        val exception = AuthSettingsException.AuthSettingsNotFoundException("settings.json")
        coEvery { dataSource.load() } throws exception

        val result = repository.getAuthSettings()

        assertTrue(result.isFailure)
        assertEquals(
            exception,
            result.exceptionOrNull()
        )
    }
}
