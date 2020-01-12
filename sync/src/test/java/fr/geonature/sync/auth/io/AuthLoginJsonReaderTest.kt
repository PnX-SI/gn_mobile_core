package fr.geonature.sync.auth.io

import fr.geonature.commons.util.IsoDateUtils.toDate
import fr.geonature.sync.FixtureHelper.getFixture
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.AuthUser
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
    fun testReadInputFromInvalidJsonString() {
        // when read an invalid JSON as AuthLogin
        val authLogin = authLoginJsonReader.read("")

        // then
        assertNull(authLogin)
    }

    @Test
    fun testReadEmptyAuthLogin() {
        // when read an empty JSON as AuthLogin
        val authLogin = authLoginJsonReader.read("{}")

        // then
        assertNull(authLogin)
    }

    @Test
    fun testReadAuthLogin() {
        // given an auth login JSON file sample to read
        val json = getFixture("auth_login.json")

        // when parsing this file as AuthLogin
        val authLogin = authLoginJsonReader.read(json)

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
