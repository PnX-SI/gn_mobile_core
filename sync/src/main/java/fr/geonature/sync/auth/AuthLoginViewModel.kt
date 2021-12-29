package fr.geonature.sync.auth

import android.app.Application
import android.text.TextUtils
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.geonature.commons.fp.Failure
import fr.geonature.sync.R
import fr.geonature.sync.api.IGeoNatureAPIClient
import fr.geonature.sync.api.model.AuthLogin
import kotlinx.coroutines.launch

/**
 * Login view model.
 *
 * @author S. Grimault
 */
class AuthLoginViewModel(
    application: Application,
    private val authManager: IAuthManager,
    private val geoNatureAPIClient: IGeoNatureAPIClient
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
        if (!geoNatureAPIClient.checkSettings()) {
            _loginResult.value = LoginResult(error = R.string.login_failed_server_url_configuration)
            return
        }

        viewModelScope.launch {
            val authLogin = authManager.login(
                username,
                password,
                applicationId
            )

            _loginResult.value = authLogin.fold({
                when (it) {
                    is AuthFailure -> {
                        when (it.authLoginError.type) {
                            "login" -> LoginResult(error = R.string.login_failed_login)
                            "password" -> LoginResult(error = R.string.login_failed_password)
                            else -> LoginResult(error = R.string.login_failed)
                        }
                    }
                    is Failure.NetworkFailure -> {
                        LoginResult(error = R.string.snackbar_network_lost)
                    }
                    else -> LoginResult(error = R.string.login_failed)
                }
            },
                {
                    LoginResult(success = it)
                }) as LoginResult
        }
    }

    fun loginDataChanged(
        username: String,
        password: String
    ) {
        if (!isUserNameValid(username)) {
            _loginFormState.value = LoginFormState(usernameError = R.string.login_form_username_invalid)
            return
        }

        if (!isPasswordValid(password)) {
            _loginFormState.value = LoginFormState(passwordError = R.string.login_form_password_invalid)
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
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    data class LoginFormState(
        @StringRes val usernameError: Int? = null,
        @StringRes val passwordError: Int? = null,
        val isValid: Boolean = false
    )

    /**
     * Authentication result: success (user details) or error message.
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    data class LoginResult(
        val success: AuthLogin? = null,
        @StringRes val error: Int? = null
    ) {

        fun hasError(): Boolean {
            return error != null
        }
    }

    /**
     * Default Factory to use for [AuthLoginViewModel].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    class Factory(val creator: () -> AuthLoginViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthLoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return creator() as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
