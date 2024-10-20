package fr.geonature.datasync.auth

import androidx.lifecycle.LiveData
import fr.geonature.commons.error.Failure
import fr.geonature.commons.fp.Either
import fr.geonature.datasync.api.model.AuthLogin

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
     * Gets the logged in user.
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
}