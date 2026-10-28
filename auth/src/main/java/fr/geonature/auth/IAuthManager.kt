package fr.geonature.auth

import androidx.lifecycle.LiveData
import fr.geonature.api.model.AuthLogin
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either

/**
 * [AuthLogin] manager.
 *
 * @author S. Grimault
 */
interface IAuthManager {

    /**
     * Check if the current session is still valid.
     */
    val isLoggedIn: LiveData<AuthLogin?>

    /**
     * Gets the logged-in user.
     */
    suspend fun getAuthLogin(): AuthLogin?

    /**
     * Performs authentication.
     */
    suspend fun login(
        username: String,
        password: String,
        applicationId: Int
    ): Either<Failure, AuthLogin>

    /**
     * Clears the current session.
     */
    suspend fun logout(): Boolean

    companion object {

        /**
         * The notification ID used to notify the user that authentication is required.
         */
        const val AUTH_NOTIFICATION_ID = 4
    }
}
