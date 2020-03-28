package fr.geonature.sync.auth.io

import android.app.Application
import fr.geonature.commons.util.IsoDateUtils.toDate
import fr.geonature.sync.FixtureHelper.getFixture
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.AuthUser
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests about [AuthLoginJsonWriter].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
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
        val json = authLoginJsonWriter.setIndent("  ")
            .write(authLogin)

        // then
        assertNotNull(json)
        Assert.assertEquals(
            getFixture("auth_login.json"),
            json
        )
    }
}
