package fr.geonature.sync.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.commons.util.afterTextChanged
import fr.geonature.commons.util.observeOnce
import fr.geonature.sync.MainApplication
import fr.geonature.sync.R
import fr.geonature.sync.auth.AuthLoginViewModel
import fr.geonature.sync.settings.AppSettings
import fr.geonature.sync.settings.AppSettingsViewModel

/**
 * Login Activity.
 *
 * @author S. Grimault
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var authLoginViewModel: AuthLoginViewModel

    private var appSettings: AppSettings? = null

    private var content: ConstraintLayout? = null
    private var editTextUsername: TextInputLayout? = null
    private var editTextPassword: TextInputLayout? = null
    private var buttonLogin: Button? = null
    private var progress: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        authLoginViewModel = ViewModelProvider(this,
            AuthLoginViewModel.Factory {
                AuthLoginViewModel(
                    application,
                    (application as MainApplication).sl.authManager,
                    (application as MainApplication).sl.geoNatureAPIClient
                )
            })[AuthLoginViewModel::class.java].apply {
                loginFormState.observe(this@LoginActivity,
                    {
                        val loginState = it
                            ?: return@observe

                        // disable login button unless both username / password is valid
                        buttonLogin?.isEnabled = loginState.isValid && appSettings != null

                        editTextUsername?.error = if (loginState.usernameError == null) null else getString(loginState.usernameError)
                        editTextPassword?.error = if (loginState.passwordError == null) null else getString(loginState.passwordError)
                    })

                loginResult.observe(this@LoginActivity,
                    {
                        val loginResult = it
                            ?: return@observe

                        progress?.visibility = View.GONE

                        if (loginResult.hasError()) {
                            showToast(
                                loginResult.error
                                    ?: R.string.login_failed
                            )

                            return@observe
                        }

                        showToast(R.string.login_success)

                        // Complete and destroy login activity once successful
                        setResult(RESULT_OK)
                        finish()
                    })
            }

        content = findViewById(R.id.content)
        progress = findViewById(android.R.id.progress)

        editTextUsername = findViewById(R.id.edit_text_username)
        editTextUsername?.apply {
            editText?.afterTextChanged {
                authLoginViewModel.loginDataChanged(
                    editTextUsername?.editText?.text.toString(),
                    editTextPassword?.editText?.text.toString()
                )
            }
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    authLoginViewModel.loginDataChanged(
                        editTextUsername?.editText?.text.toString(),
                        editTextPassword?.editText?.text.toString()
                    )
                }
            }
        }

        editTextPassword = findViewById(R.id.edit_text_password)
        editTextPassword?.apply {
            editText?.afterTextChanged {
                authLoginViewModel.loginDataChanged(
                    editTextUsername?.editText?.text.toString(),
                    editTextPassword?.editText?.text.toString()
                )
            }
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    authLoginViewModel.loginDataChanged(
                        editTextUsername?.editText?.text.toString(),
                        editTextPassword?.editText?.text.toString()
                    )
                }
            }
            editText?.setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> performLogin(
                        editTextUsername?.editText?.text.toString(),
                        editTextPassword?.editText?.text.toString()
                    )
                }

                false
            }
        }

        buttonLogin = findViewById(R.id.button_login)
        buttonLogin?.setOnClickListener {
            performLogin(
                editTextUsername?.editText?.text.toString(),
                editTextPassword?.editText?.text.toString()
            )
        }

        loadAppSettings()
    }

    private fun loadAppSettings() {
        ViewModelProvider(this,
            fr.geonature.commons.settings.AppSettingsViewModel.Factory {
                AppSettingsViewModel((application as MainApplication).sl.appSettingsManager)
            })[AppSettingsViewModel::class.java].also { vm ->
            vm
                .loadAppSettings()
                .observeOnce(this) {
                    if (it == null) {
                        makeSnackbar(
                            getString(
                                R.string.snackbar_settings_not_found,
                                vm.getAppSettingsFilename()
                            )
                        )
                            ?.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                override fun onDismissed(
                                    transientBottomBar: Snackbar?,
                                    event: Int
                                ) {
                                    super.onDismissed(
                                        transientBottomBar,
                                        event
                                    )

                                    setResult(RESULT_CANCELED)
                                    finish()
                                }
                            })
                            ?.show()
                    } else {
                        appSettings = it
                    }
                }
        }
    }

    private fun performLogin(
        username: String,
        password: String
    ) {
        val appSettings = appSettings
            ?: return

        editTextPassword?.also {
            hideSoftKeyboard(it)
        }
        progress?.visibility = View.VISIBLE

        authLoginViewModel.login(
            username,
            password,
            appSettings.applicationId
        )
    }

    private fun showToast(
        @StringRes messageResourceId: Int
    ) {
        Toast
            .makeText(
                applicationContext,
                messageResourceId,
                Toast.LENGTH_LONG
            )
            .show()
    }

    private fun makeSnackbar(text: CharSequence): Snackbar? {
        val view = content
            ?: return null

        return Snackbar.make(
            view,
            text,
            Snackbar.LENGTH_LONG
        )
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(
                context,
                LoginActivity::class.java
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
        }
    }
}
