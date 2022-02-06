package fr.geonature.datasync.auth.io

import fr.geonature.commons.util.toDate
import fr.geonature.datasync.FixtureHelper.getFixture
import fr.geonature.datasync.api.model.AuthLogin
import fr.geonature.datasync.api.model.AuthUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [AuthLoginJsonWriter].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class AuthLoginJsonWriterTest {

    private val authLoginJsonWriter = AuthLoginJsonWriter()

    @Test
    fun testWriteAuthLogin() {
        // given an AuthLogin instance
        val authLogin = AuthLogin(
            AuthUser(
                1234L,
                "Admin",
                "Test",
                3,
                1,
                "admin"
            ),
            toDate("2019-11-19T09:30:00Z")!!
        )

        // when write this AuthLogin as JSON string
        val json = authLoginJsonWriter
            .setIndent("  ")
            .write(authLogin)

        // then
        assertNotNull(json)
        assertEquals(
            getFixture("auth_login.json"),
            json
        )
    }
}
