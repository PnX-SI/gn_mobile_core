package fr.geonature.sync.ui.login

import android.app.Application
import android.text.TextUtils
import androidx.annotation.StringRes
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
import fr.geonature.sync.auth.AuthManager
import fr.geonature.sync.util.SettingsUtils
import kotlinx.coroutines.launch
import retrofit2.Response

/**
 * Login view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class LoginViewModel(application: Application) : ViewModel() {

    private val authManager: AuthManager = AuthManager(application)
    private var geoNatureAPIClient: GeoNatureAPIClient? = null

    init {
        SettingsUtils.getGeoNatureServerUrl(application)
                ?.also {
                    geoNatureAPIClient = GeoNatureAPIClient.instance(application,
                                                                     it)
                            .value
                }
    }

    private val _loginFormState = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginFormState

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String,
              password: String,
              applicationId: Int) {
        val geoNatureAPIClient = geoNatureAPIClient ?: return

        viewModelScope.launch {
            val authLoginResponse = geoNatureAPIClient.authLogin(AuthCredentials(username,
                                                                                 password,
                                                                                 applicationId))

            if (!authLoginResponse.isSuccessful) {
                val authLoginError = buildErrorResponse(authLoginResponse)

                _loginResult.value = if (authLoginError != null) {
                    when (authLoginError.type) {
                        "login" -> LoginResult(error = R.string.login_failed_login)
                        "password" -> LoginResult(error = R.string.login_failed_password)
                        else -> LoginResult(error = R.string.login_failed)
                    }
                }
                else {
                    LoginResult(error = R.string.login_failed)
                }

                return@launch
            }

            val authLogin = authLoginResponse.body()

            if (authLogin == null) {
                _loginResult.value = LoginResult(error = R.string.login_failed)
                return@launch
            }

            authManager.setAuthLogin(authLogin)
            _loginResult.value = LoginResult(success = authLogin)
        }
    }

    fun loginDataChanged(username: String,
                         password: String) {
        if (!isUserNameValid(username)) {
            _loginFormState.value = LoginFormState(usernameError = R.string.login_form_username_invalid)
            return
        }

        if (!isPasswordValid(password)) {
            _loginFormState.value = LoginFormState(passwordError = R.string.login_form_password_invalid)
            return
        }

        _loginFormState.value = LoginFormState(isValid = true)
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

        return Gson().fromJson(response.errorBody()!!.charStream(),
                               type)
    }

    /**
     * Data validation state of the login form.
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    data class LoginFormState(@StringRes
                              val usernameError: Int? = null,
                              @StringRes
                              val passwordError: Int? = null, val isValid: Boolean = false)

    /**
     * Authentication result: success (user details) or errorResourceId message.
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    data class LoginResult(val success: AuthLogin? = null,
                           @StringRes
                           val error: Int? = null) {

        fun hasError(): Boolean {
            return error != null
        }
    }

    /**
     * Default Factory to use for [LoginViewModel].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    class Factory(val creator: () -> LoginViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST") return creator() as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
