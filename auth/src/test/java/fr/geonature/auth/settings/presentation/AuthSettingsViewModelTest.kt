package fr.geonature.auth.settings.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.auth.CoroutineTestRule
import fr.geonature.auth.settings.domain.AuthSettings
import fr.geonature.auth.settings.error.AuthSettingsException
import fr.geonature.auth.settings.repository.IAuthSettingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [AuthSettingsViewModel].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class AuthSettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val repository = mockk<IAuthSettingsRepository>()

    @Test
    fun `should expose loaded settings`() {
        coEvery { repository.getAuthSettings() } returns Result.success(AuthSettings(applicationId = 3))

        var result: Result<AuthSettings>? = null
        AuthSettingsViewModel(repository)
            .getAuthSettings()
            .observeForever { result = it }

        assertEquals(
            AuthSettings(applicationId = 3),
            result?.getOrNull()
        )
    }

    @Test
    fun `should expose failure`() {
        coEvery { repository.getAuthSettings() } returns Result.failure(AuthSettingsException.AuthSettingsNotFoundException())

        var result: Result<AuthSettings>? = null
        AuthSettingsViewModel(repository)
            .getAuthSettings()
            .observeForever { result = it }

        assertTrue(result?.exceptionOrNull() is AuthSettingsException.AuthSettingsNotFoundException)
    }
}
