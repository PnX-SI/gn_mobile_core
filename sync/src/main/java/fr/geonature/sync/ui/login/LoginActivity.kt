package fr.geonature.sync.ui.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.commons.util.afterTextChanged
import fr.geonature.sync.R
import fr.geonature.sync.auth.AuthLoginViewModel
import fr.geonature.sync.settings.AppSettings
import fr.geonature.sync.settings.AppSettingsViewModel
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var appSettingsViewModel: AppSettingsViewModel
    private lateinit var authLoginViewModel: AuthLoginViewModel
    private var appSettings: AppSettings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        appSettingsViewModel = ViewModelProvider(this,
                                                 fr.geonature.commons.settings.AppSettingsViewModel.Factory { AppSettingsViewModel(application) }).get(AppSettingsViewModel::class.java)

        loadAppSettings()

        authLoginViewModel = ViewModelProvider(this,
                                               AuthLoginViewModel.Factory { AuthLoginViewModel(application) }).get(AuthLoginViewModel::class.java)
                .apply {
                    loginFormState.observe(this@LoginActivity,
                                           Observer {
                                               val loginState = it ?: return@Observer

                                               // disable login button unless both username / password is valid
                                               button_login.isEnabled = loginState.isValid && appSettings != null

                                               if (loginState.usernameError != null) {
                                                   edit_text_username.error = getString(loginState.usernameError)
                                               }

                                               if (loginState.passwordError != null) {
                                                   edit_text_password.error = getString(loginState.passwordError)
                                               }
                                           })

                    loginResult.observe(this@LoginActivity,
                                        Observer {
                                            val loginResult = it ?: return@Observer

                                            progress.visibility = View.GONE

                                            if (loginResult.hasError()) {
                                                showToast(loginResult.error
                                                              ?: R.string.login_failed)

                                                return@Observer
                                            }

                                            showToast(R.string.login_success)
                                            setResult(Activity.RESULT_OK)

                                            // Complete and destroy login activity once successful
                                            finish()
                                        })
                }

        edit_text_username.afterTextChanged {
            authLoginViewModel.loginDataChanged(edit_text_username.text.toString(),
                                                edit_text_password.text.toString())
        }

        edit_text_password.apply {
            afterTextChanged {
                authLoginViewModel.loginDataChanged(edit_text_username.text.toString(),
                                                    edit_text_password.text.toString())
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> performLogin(edit_text_username.text.toString(),
                                                               edit_text_password.text.toString())
                }

                false
            }
        }

        button_login.setOnClickListener {
            performLogin(edit_text_username.text.toString(),
                         edit_text_password.text.toString())
        }
    }

    private fun loadAppSettings() {
        appSettingsViewModel.getAppSettings<AppSettings>()
                .observe(this,
                         Observer {
                             if (it == null) {
                                 Snackbar.make(content,
                                               getString(R.string.snackbar_settings_not_found,
                                                         appSettingsViewModel.getAppSettingsFilename()),
                                               Snackbar.LENGTH_LONG)
                                         .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                             override fun onDismissed(transientBottomBar: Snackbar?,
                                                                      event: Int) {
                                                 super.onDismissed(transientBottomBar,
                                                                   event)

                                                 finish()
                                             }
                                         })
                                         .show()

                             }
                             else {
                                 appSettings = it
                             }
                         })
    }

    private fun performLogin(username: String,
                             password: String) {
        val appSettings = appSettings ?: return

        hideSoftKeyboard(edit_text_password)
        progress.visibility = View.VISIBLE

        authLoginViewModel.login(username,
                                 password,
                                 appSettings.applicationId)
    }

    private fun showToast(@StringRes
                          messageResourceId: Int) {
        Toast.makeText(applicationContext,
                       messageResourceId,
                       Toast.LENGTH_LONG)
                .show()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context,
                          LoginActivity::class.java)
        }
    }
}
