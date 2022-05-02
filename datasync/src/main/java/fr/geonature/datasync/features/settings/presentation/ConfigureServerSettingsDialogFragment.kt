package fr.geonature.datasync.features.settings.presentation

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.lifecycle.observe
import fr.geonature.commons.util.KeyboardUtils.hideSoftKeyboard
import fr.geonature.commons.util.KeyboardUtils.showSoftKeyboard
import fr.geonature.commons.util.afterTextChanged
import fr.geonature.datasync.R

/**
 * Custom [Dialog] used to edit server URL.
 *
 * @author S. Grimault
 */
@AndroidEntryPoint
class ConfigureServerSettingsDialogFragment : DialogFragment() {

    private val configureServerSettingsViewModel: ConfigureServerSettingsViewModel by viewModels()

    private var editTextServerUrl: TextInputLayout? = null
    private var buttonValidate: Button? = null
    private var onConfigureServerSettingsDialogFragmentListener: OnConfigureServerSettingsDialogFragmentListener? =
        null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(configureServerSettingsViewModel) {
            observe(
                formState,
                ::handleFormState
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()

        val view = View.inflate(
            context,
            R.layout.dialog_server_settings,
            null,
        )
        editTextServerUrl = view
            .findViewById<TextInputLayout>(R.id.edit_text_server_url)
            ?.also { textInputLayout ->
                textInputLayout.editText?.afterTextChanged {
                    configureServerSettingsViewModel.validateForm(it?.toString())
                }
                textInputLayout.setOnFocusChangeListener { view, hasFocus ->
                    if (view.isDirty && hasFocus) {
                        configureServerSettingsViewModel.validateForm(textInputLayout.editText?.text?.toString())
                    }
                }
                textInputLayout.editText?.setOnEditorActionListener { _, actionId, _ ->
                    when (actionId) {
                        EditorInfo.IME_ACTION_DONE -> configureServerSettingsViewModel.validateForm(
                            textInputLayout.editText?.text?.toString(),
                            submitted = true
                        )
                    }

                    false
                }
            }

        arguments
            ?.getString(KEY_SERVER_URL)
            ?.also {
                editTextServerUrl?.editText?.text = Editable.Factory
                    .getInstance()
                    .newEditable(it.removePrefix("https://"))
            }

        // restore the previous state if any
        savedInstanceState
            ?.getString(KEY_SERVER_URL)
            ?.also {
                editTextServerUrl?.editText?.text = Editable.Factory
                    .getInstance()
                    .newEditable(it.removePrefix("https://"))
            }

        val alertDialog = AlertDialog
            .Builder(context)
            .setTitle(R.string.dialog_edit_server_settings_title)
            .setView(view)
            .setPositiveButton(R.string.alert_dialog_ok) { _, _ ->
                editTextServerUrl?.editText?.let { hideSoftKeyboard(it) }
                configureServerSettingsViewModel.validateForm(
                    editTextServerUrl?.editText?.text?.toString(),
                    submitted = true
                )
            }
            .setNegativeButton(
                R.string.alert_dialog_cancel,
                null
            )
            .create()

        alertDialog.setOnShowListener {
            buttonValidate = (it as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
            configureServerSettingsViewModel.validateForm(editTextServerUrl?.editText?.text?.toString())

            // show automatically the soft keyboard for the EditText
            editTextServerUrl?.editText?.let { view -> showSoftKeyboard(view) }
        }

        return alertDialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(
            KEY_SERVER_URL,
            editTextServerUrl?.editText?.text?.toString()
        )

        super.onSaveInstanceState(outState)
    }

    fun setOnConfigureServerSettingsDialogFragmentListener(
        onConfigureServerSettingsDialogFragmentListener: OnConfigureServerSettingsDialogFragmentListener
    ) {
        this.onConfigureServerSettingsDialogFragmentListener =
            onConfigureServerSettingsDialogFragmentListener
    }

    private fun handleFormState(formState: ConfigureServerSettingsViewModel.FormState) {
        when (formState) {
            is ConfigureServerSettingsViewModel.FormState.FormStateError -> {
                // disable validate button unless GeoNature server URL is valid
                buttonValidate?.isEnabled = false

                // show error message
                editTextServerUrl?.error = getString(formState.error)
            }
            is ConfigureServerSettingsViewModel.FormState.FormStateValid -> {
                buttonValidate?.isEnabled = true

                // clear error message
                editTextServerUrl?.error = null
            }
            is ConfigureServerSettingsViewModel.FormState.FormStateSubmitted -> {
                onConfigureServerSettingsDialogFragmentListener?.onChanged(formState.serverBaseUrl)
            }
        }
    }

    companion object {

        const val KEY_SERVER_URL = "server_url"

        /**
         * Use this factory method to create a new instance of [ConfigureServerSettingsDialogFragment].
         *
         * @return A new instance of [ConfigureServerSettingsDialogFragment]
         */
        fun newInstance(url: String?) =
            ConfigureServerSettingsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(
                        KEY_SERVER_URL,
                        url
                    )
                }
            }
    }

    /**
     * The callback used by [ConfigureServerSettingsDialogFragment].
     *
     * @author S. Grimault
     */
    interface OnConfigureServerSettingsDialogFragmentListener {

        /**
         * Invoked when the positive button of the dialog is pressed.
         *
         * @param url the new server URL edited from this dialog
         */
        fun onChanged(url: String)
    }
}