package fr.geonature.auth.settings.io

import fr.geonature.auth.FixtureHelper.getFixture
import fr.geonature.auth.settings.domain.AuthSettings
import fr.geonature.auth.settings.error.AuthSettingsException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.StringReader

/**
 * Unit tests about [AuthSettingsJsonReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class AuthSettingsJsonReaderTest {

    private val authSettingsJsonReader = AuthSettingsJsonReader()

    @Test
    fun `should read settings from anonymous object`() {
        assertEquals(
            AuthSettings(applicationId = 3),
            authSettingsJsonReader.read(getFixture("settings_auth.json"))
        )
    }

    @Test
    fun `should read settings from 'auth' object`() {
        assertEquals(
            AuthSettings(applicationId = 3),
            authSettingsJsonReader.read(getFixture("settings_auth_nested.json"))
        )
    }

    @Test
    fun `should read settings from legacy property`() {
        assertEquals(
            AuthSettings(applicationId = 3),
            authSettingsJsonReader.read(getFixture("settings_auth_legacy.json"))
        )
    }

    @Test
    fun `should read settings from 'sync' object`() {
        assertEquals(
            AuthSettings(applicationId = 3),
            authSettingsJsonReader.read(getFixture("settings_auth_from_sync.json"))
        )
    }

    @Test
    fun `should prefer 'auth' object over 'sync' object`() {
        assertEquals(
            AuthSettings(applicationId = 5),
            authSettingsJsonReader.read("""{"sync": {"gn_application_id": 3}, "auth": {"gn_application_id": 5}}""")
        )
    }

    @Test
    fun `should use existing settings as default`() {
        assertEquals(
            AuthSettings(applicationId = 7),
            AuthSettingsJsonReader(AuthSettings(applicationId = 7)).read("{}")
        )
    }

    @Test
    fun `should return default settings from empty object`() {
        assertEquals(
            AuthSettings(),
            authSettingsJsonReader.read("{}")
        )
    }

    @Test
    fun `should return null from empty or blank JSON string`() {
        assertNull(authSettingsJsonReader.read(null))
        assertNull(authSettingsJsonReader.read(""))
        assertNull(authSettingsJsonReader.read("  "))
    }

    @Test
    fun `should return null from invalid JSON string`() {
        assertNull(authSettingsJsonReader.read("{"))
    }

    @Test(expected = AuthSettingsException.AuthSettingsJsonParseException::class)
    fun `should throw AuthSettingsJsonParseException from invalid JSON`() {
        authSettingsJsonReader.read(StringReader("""{"gn_application_id": "abc"}"""))
    }
}
