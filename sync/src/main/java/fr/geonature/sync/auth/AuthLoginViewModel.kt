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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.geonature.sync.R
import fr.geonature.sync.api.GeoNatureAPIClient
import fr.geonature.sync.api.model.AuthCredentials
import fr.geonature.sync.api.model.AuthLogin
import fr.geonature.sync.api.model.AuthLoginError
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Login view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AuthLoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager: AuthManager = AuthManager.getInstance(application)
    private val geoNatureAPIClient: GeoNatureAPIClient? = GeoNatureAPIClient.instance(application)

    private val _loginFormState = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginFormState

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    val isLoggedIn: LiveData<Boolean> = authManager.isLoggedIn

    init {
        if (geoNatureAPIClient == null) {
            _loginResult.value = LoginResult(error = R.string.login_failed_server_url_configuration)
            _loginFormState.value = LoginFormState(
                isValid = false,
                usernameError = null,
                passwordError = null
            )
        }
    }

    fun login(
        username: String,
        password: String,
        applicationId: Int
    ) {
        if (geoNatureAPIClient == null) {
            _loginResult.value = LoginResult(error = R.string.login_failed_server_url_configuration)
            return
        }

        viewModelScope.launch {
            try {
                val authLoginResponse = geoNatureAPIClient.authLogin(
                    AuthCredentials(
                        username,
                        password,
                        applicationId
                    )
                )

                if (!authLoginResponse.isSuccessful) {
                    val authLoginError = buildErrorResponse(authLoginResponse)

                    _loginResult.value = if (authLoginError != null) {
                        when (authLoginError.type) {
                            "login" -> LoginResult(error = R.string.login_failed_login)
                            "password" -> LoginResult(error = R.string.login_failed_password)
                            else -> LoginResult(error = R.string.login_failed)
                        }
                    } else {
                        LoginResult(error = R.string.login_failed)
                    }

                    return@launch
                }

                val authLogin = authLoginResponse.body()

                if (authLogin == null) {
                    _loginResult.value = LoginResult(error = R.string.login_failed)
                    return@launch
                }

                authManager.setAuthLogin(authLogin).also {
                    _loginResult.value = LoginResult(success = authLogin)
                }
            } catch (e: Exception) {
                _loginResult.value = LoginResult(error = R.string.login_failed)
            }
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
        val disconnected = MutableLiveData<Boolean>()

        viewModelScope.launch {
            disconnected.value = authManager.logout()
        }

        return disconnected
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return !TextUtils.isEmpty(username)
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return !TextUtils.isEmpty(password)
    }

    private fun buildErrorResponse(response: Response<AuthLogin>): AuthLoginError? {
        val type = object : TypeToken<AuthLoginError>() {}.type

        return Gson().fromJson(
            response.errorBody()!!.charStream(),
            type
        )
    }

    /**
     * Data validation state of the login form.
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    data class LoginFormState(
        @StringRes
        val usernameError: Int? = null,
        @StringRes
        val passwordError: Int? = null,
        val isValid: Boolean = false
    )

    /**
     * Authentication result: success (user details) or errorResourceId message.
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    data class LoginResult(
        val success: AuthLogin? = null,
        @StringRes
        val error: Int? = null
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
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthLoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return creator() as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
