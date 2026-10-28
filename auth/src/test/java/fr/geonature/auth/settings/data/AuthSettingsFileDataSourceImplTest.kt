package fr.geonature.auth.settings.data

import fr.geonature.auth.FixtureHelper.getFixtureAsFile
import fr.geonature.auth.settings.domain.AuthSettings
import fr.geonature.auth.settings.error.AuthSettingsException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Unit tests about [AuthSettingsFileDataSourceImpl].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AuthSettingsFileDataSourceImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `should load settings from file`() = runTest {
        val dataSource = AuthSettingsFileDataSourceImpl(
            getFixtureAsFile("settings_auth.json"),
            StandardTestDispatcher(testScheduler)
        )

        assertEquals(
            AuthSettings(applicationId = 3),
            dataSource.load()
        )
    }

    @Test
    fun `should throw AuthSettingsNotFoundException if file does not exist`() = runTest {
        val missing = File(
            temporaryFolder.root,
            "missing.json"
        )
        val dataSource = AuthSettingsFileDataSourceImpl(
            missing,
            StandardTestDispatcher(testScheduler)
        )

        val result = runCatching { dataSource.load() }
        assertTrue(result.isFailure)

        val exception = result.exceptionOrNull()
        assertTrue(exception is AuthSettingsException.AuthSettingsNotFoundException)
        assertEquals(
            missing.absolutePath,
            (exception as AuthSettingsException.AuthSettingsNotFoundException).source
        )
    }

    @Test
    fun `should throw AuthSettingsJsonParseException if file is invalid`() = runTest {
        val invalid = temporaryFolder
            .newFile("invalid.json")
            .also { it.writeText("""{"gn_application_id": "abc"}""") }
        val dataSource = AuthSettingsFileDataSourceImpl(
            invalid,
            StandardTestDispatcher(testScheduler)
        )

        val result = runCatching { dataSource.load() }
        assertTrue(result.isFailure)

        val exception = result.exceptionOrNull()
        assertTrue(exception is AuthSettingsException.AuthSettingsJsonParseException)
        assertTrue(exception?.message != null)
    }
}
