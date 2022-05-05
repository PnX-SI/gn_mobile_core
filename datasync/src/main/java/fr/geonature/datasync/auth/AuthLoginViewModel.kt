package fr.geonature.datasync.auth

import android.app.Application
import android.text.TextUtils
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.commons.error.Failure
import fr.geonature.datasync.R
import fr.geonature.datasync.api.GeoNatureMissingConfigurationFailure
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.api.model.AuthLogin
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Login view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class AuthLoginViewModel @Inject constructor(
    application: Application,
    private val authManager: IAuthManager,
    geoNatureAPIClient: IGeoNatureAPIClient
) : AndroidViewModel(application) {

    private val _loginFormState = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginFormState

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    val isLoggedIn: LiveData<Boolean> = authManager.isLoggedIn

    init {
        if (!geoNatureAPIClient.checkSettings()) {
            _loginResult.value = LoginResult(error = R.string.login_failed_server_url_configuration)
            _loginFormState.value = LoginFormState(
                isValid = false,
                usernameError = null,
                passwordError = null
            )
        }
    }

    fun checkAuthLogin(): LiveData<AuthLogin?> {
        val authLoginLiveData = MutableLiveData<AuthLogin?>()

        viewModelScope.launch {
            val authLogin = authManager.getAuthLogin()
            authLoginLiveData.postValue(authLogin)
        }

        return authLoginLiveData
    }

    fun login(
        username: String,
        password: String,
        applicationId: Int
    ) {
        viewModelScope.launch {
            val authLogin = authManager.login(
                username,
                password,
                applicationId
            )

            _loginResult.value = authLogin.fold({
                when (it) {
                    is GeoNatureMissingConfigurationFailure -> {
                        LoginResult(error = R.string.login_failed_server_url_configuration)
                    }
                    is AuthFailure -> {
                        when (it.authLoginError.type) {
                            "login" -> LoginResult(error = R.string.login_failed_login)
                            "password" -> LoginResult(error = R.string.login_failed_password)
                            else -> LoginResult(error = R.string.login_failed)
                        }
                    }
                    is Failure.NetworkFailure -> {
                        LoginResult(error = R.string.error_network_lost)
                    }
                    else -> LoginResult(error = R.string.login_failed)
                }
            },
                {
                    LoginResult(success = it)
                })
        }
    }

    fun loginDataChanged(
        username: String,
        password: String
    ) {
        if (!isUserNameValid(username)) {
            _loginFormState.value =
                LoginFormState(usernameError = R.string.login_form_username_invalid)
            return
        }

        if (!isPasswordValid(password)) {
            _loginFormState.value =
                LoginFormState(passwordError = R.string.login_form_password_invalid)
            return
        }

        _loginFormState.value = LoginFormState(
            isValid = true,
            usernameError = null,
            passwordError = null
        )
    }

    fun logout(): LiveData<Boolean> {
        val disconnectedLiveData = MutableLiveData<Boolean>()

        viewModelScope.launch {
            val disconnected = authManager.logout()
            disconnectedLiveData.value = disconnected
        }

        return disconnectedLiveData
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return !TextUtils.isEmpty(username)
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return !TextUtils.isEmpty(password)
    }

    /**
     * Data validation state of the login form.
     *
     * @author S. Grimault
     */
    data class LoginFormState(
        @StringRes val usernameError: Int? = null,
        @StringRes val passwordError: Int? = null,
        val isValid: Boolean = false
    )

    /**
     * Authentication result: success (user details) or error message.
     *
     * @author S. Grimault
     */
    data class LoginResult(
        val success: AuthLogin? = null,
        @StringRes val error: Int? = null
    ) {

        fun hasError(): Boolean {
            return error != null
        }
    }
}
