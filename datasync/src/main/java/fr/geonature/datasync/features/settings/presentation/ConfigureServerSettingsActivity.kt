package fr.geonature.datasync.features.settings.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.error.Failure
import fr.geonature.commons.lifecycle.observe
import fr.geonature.commons.lifecycle.onFailure
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.commons.util.ThemeUtils.getErrorColor
import fr.geonature.commons.util.afterTextChanged
import fr.geonature.datasync.R
import fr.geonature.datasync.packageinfo.error.PackageInfoNotFoundFromRemoteFailure
import fr.geonature.datasync.settings.error.DataSyncSettingsNotFoundFailure

/**
 * Configure server settings for the first time.
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class ConfigureServerSettingsActivity : AppCompatActivity() {

    private val configureServerSettingsViewModel: ConfigureServerSettingsViewModel by viewModels()

    private var content: ConstraintLayout? = null
    private var editTextServerUrl: TextInputLayout? = null
    private var buttonValidate: Button? = null
    private var progress: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_server_settings)

        with(configureServerSettingsViewModel) {
            observe(
                formState,
                ::handleFormState,
            )
            observe(dataSyncSettingLoaded) { dataSyncSettingsLoaded() }
            onFailure(
                failure,
                ::handleFailure,
            )
        }

        content = findViewById(R.id.content)
        progress = findViewById(android.R.id.progress)
        editTextServerUrl = findViewById(R.id.edit_text_server_url)
        buttonValidate = findViewById(R.id.button_validate)

        editTextServerUrl?.apply {
            editText?.afterTextChanged {
                configureServerSettingsViewModel.validateForm(editTextServerUrl?.editText?.text.toString())
            }
            setOnFocusChangeListener { view, hasFocus ->
                if (view.isDirty && hasFocus) {
                    configureServerSettingsViewModel.validateForm(editTextServerUrl?.editText?.text.toString())
                }
            }
            editText?.setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> loadSettings(editTextServerUrl?.editText?.text.toString())
                }

                false
            }
        }

        buttonValidate?.setOnClickListener {
            loadSettings(editTextServerUrl?.editText?.text.toString())
        }
    }

    private fun loadSettings(geoNatureBaseUrl: String) {
        editTextServerUrl?.also {
            hideSoftKeyboard(it)
        }

        progress?.visibility = View.VISIBLE
        configureServerSettingsViewModel.loadAppSettings("${
            Uri.parse(geoNatureBaseUrl).scheme?.run { "" } ?: "https://"
        }$geoNatureBaseUrl")
    }

    private fun handleFormState(formState: ConfigureServerSettingsViewModel.FormState) {
        // disable validate button unless GeoNature server URL is valid
        buttonValidate?.isEnabled = formState.isValid

        // show error message
        editTextServerUrl?.error = if (formState.error == null) null else getString(formState.error)
    }

    private fun dataSyncSettingsLoaded() {
        showToast(R.string.settings_server_settings_loaded)
        setResult(RESULT_OK)
        finish()
    }

    private fun handleFailure(failure: Failure) {
        progress?.visibility = View.GONE

        when (failure) {
            is Failure.NetworkFailure -> {
                makeSnackbar(failure.reason)
                    ?.setBackgroundTint(getErrorColor(this))
                    ?.show()
            }
            is Failure.ServerFailure -> {
                makeSnackbar(getString(R.string.settings_server_error))
                    ?.setBackgroundTint(getErrorColor(this))
                    ?.show()
            }
            is PackageInfoNotFoundFromRemoteFailure -> {
                makeSnackbar(getString(R.string.settings_server_settings_not_found))
                    ?.setBackgroundTint(getErrorColor(this))
                    ?.show()
            }
            is DataSyncSettingsNotFoundFailure -> {
                makeSnackbar(if (failure.source.isNullOrBlank()) getString(R.string.error_settings_undefined) else getString(R.string.error_settings_not_found,
                    failure.source))
                    ?.setBackgroundTint(getErrorColor(this))
                    ?.show()
            }
            else -> {
                makeSnackbar(getString(R.string.error_settings_undefined))
                    ?.setBackgroundTint(getErrorColor(this))
                    ?.show()
            }
        }
    }

    private fun showToast(
        @StringRes messageResourceId: Int,
    ) {
        Toast
            .makeText(applicationContext,
                messageResourceId,
                Toast.LENGTH_LONG)
            .show()
    }

    private fun makeSnackbar(text: CharSequence): Snackbar? {
        val view = content
            ?: return null

        return Snackbar.make(view,
            text,
            Snackbar.LENGTH_LONG)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context,
                ConfigureServerSettingsActivity::class.java)
        }
    }
}