package fr.geonature.auth.settings.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Unit tests about [AuthSettings].
 *
 * @author S. Grimault
 */
class AuthSettingsTest {

    @Test
    fun `should build settings with applicationId equals to 0`() {
        assertEquals(
            AuthSettings(applicationId = 0),
            AuthSettings.Builder().applicationId(0).build()
        )
    }

    @Test
    fun `should build settings with applicationId greater than 0`() {
        assertEquals(
            AuthSettings(applicationId = 3),
            AuthSettings.Builder().applicationId(3).build()
        )
    }

    @Test
    fun `should build settings from existing settings`() {
        assertEquals(
            AuthSettings(applicationId = 5),
            AuthSettings.Builder().from(AuthSettings(applicationId = 5)).build()
        )
    }

    @Test
    fun `should throw IllegalArgumentException if applicationId is not set`() {
        assertThrows(IllegalArgumentException::class.java) {
            AuthSettings.Builder().build()
        }
    }

    @Test
    fun `should throw IllegalArgumentException if applicationId is negative`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            AuthSettings.Builder().applicationId(-1).build()
        }

        assertEquals("applicationId must be >= 0", exception.message)
    }
}
