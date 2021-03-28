package fr.geonature.sync.ui.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import fr.geonature.sync.R
import fr.geonature.sync.util.SettingsUtils.updatePreferences

/**
 * Global settings.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            configureNotifications()
        }

        updatePreferences(preferenceScreen)
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        addPreferencesFromResource(R.xml.preferences_servers)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addPreferencesFromResource(R.xml.preferences_notifications)
        }

        addPreferencesFromResource(R.xml.preferences_storage)
        addPreferencesFromResource(R.xml.preferences_about)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun configureNotifications() {
        preferenceScreen
            .findPreference<Preference>(getString(R.string.preference_category_notifications_configure_key))
            ?.apply {
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(
                            Settings.EXTRA_APP_PACKAGE,
                            context.packageName
                        )
                    })
                    true
                }
            }
    }

    companion object {

        /**
         * Use this factory method to create a new instance of [PreferencesFragment].
         *
         * @return A new instance of [PreferencesFragment]
         */
        @JvmStatic
        fun newInstance() =
            PreferencesFragment()
    }
}
