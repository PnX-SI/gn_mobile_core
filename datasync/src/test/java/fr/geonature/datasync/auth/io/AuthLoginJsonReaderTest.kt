package fr.geonature.datasync.auth.io

import fr.geonature.commons.util.toDate
import fr.geonature.datasync.FixtureHelper.getFixture
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.AuthUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [AuthLoginJsonReader].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class AuthLoginJsonReaderTest {

    private var authLoginJsonReader = AuthLoginJsonReader()

    @Test
    fun `should return null if trying to read auth login from empty string`() {
        // when reading an empty string as AuthLogin
        val authLogin = authLoginJsonReader.read("")

        // then
        assertNull(authLogin)
    }

    @Test
    fun `should return null if trying to read auth login from empty JSON string`() {
        // when reading an empty JSON as AuthLogin
        val authLogin = authLoginJsonReader.read("{}")

        // then
        assertNull(authLogin)
    }

    @Test
    fun `should return null if trying to read auth login from invalid JSON string`() {
        // when reading an invalid JSON as AuthLogin
        val authLogin = authLoginJsonReader.read(
            """{
                  "user": {
                    "id": 1234,
                    "lastname": "Admin",
                    "firstname": null,
                    "application_id": 3,
                    "organism_id": 1,
                    "login": "admin"
                  },
                  "expires": "2019-11-19T09:30:00Z"
                }""".trimIndent()
        )

        // then
        assertNull(authLogin)
    }

    @Test
    fun `should return auth login from valid JSON string`() {
        // given an auth login JSON file sample to read
        val json = getFixture("auth_login.json")

        // when parsing this file as AuthLogin
        val authLogin = authLoginJsonReader.read(json)

        // then
        assertNotNull(authLogin)
        assertEquals(
            AuthLogin(
                AuthUser(
                    1234L,
                    "Admin",
                    "Test",
                    3,
                    1,
                    "admin"
                ),
                toDate("2019-11-19T09:30:00Z")!!
            ),
            authLogin
        )
    }
}
