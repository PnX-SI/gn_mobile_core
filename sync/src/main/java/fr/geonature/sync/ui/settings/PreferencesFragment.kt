package fr.geonature.sync.ui.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.mountpoint.util.MountPointUtils
import fr.geonature.sync.BuildConfig
import fr.geonature.sync.R
import java.text.DateFormat
import java.util.Date

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

        updatePreferences(
            preferenceScreen,
            arguments?.getParcelable(ARG_SERVER_URLS)
        )
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

    private fun updatePreferences(
        preferenceScreen: PreferenceScreen,
        serverUrls: IGeoNatureAPIClient.ServerUrls?
    ) {
        val context = preferenceScreen.context
        val onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString()
                true
            }

        preferenceScreen
            .findPreference<EditTextPreference?>(context.getString(R.string.preference_category_server_geonature_url_key))
            ?.apply {
                setOnBindEditTextListener { it.setSingleLine() }
                summary = serverUrls?.geoNatureBaseUrl
                setOnPreferenceChangeListener(onPreferenceChangeListener)
            }
        preferenceScreen
            .findPreference<EditTextPreference?>(context.getString(R.string.preference_category_server_taxhub_url_key))
            ?.apply {
                setOnBindEditTextListener { it.setSingleLine() }
                summary = serverUrls?.taxHubBaseUrl
                setOnPreferenceChangeListener(onPreferenceChangeListener)
            }

        preferenceScreen.findPreference<Preference?>(context.getString(R.string.preference_category_storage_internal_key))?.summary =
            MountPointUtils.getInternalStorage(preferenceScreen.context).mountPath.absolutePath
        MountPointUtils
            .getExternalStorage(
                preferenceScreen.context,
                Environment.MEDIA_MOUNTED,
                Environment.MEDIA_MOUNTED_READ_ONLY
            )
            ?.also { mountPoint ->
                preferenceScreen
                    .findPreference<Preference?>(context.getString(R.string.preference_category_storage_external_key))
                    ?.also {
                        it.summary = mountPoint.mountPath.absolutePath
                        it.isEnabled = true
                    }
            }

        preferenceScreen.findPreference<Preference?>(context.getString(R.string.preference_category_about_app_version_key))?.summary =
            context.getString(
                R.string.app_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                DateFormat
                    .getDateTimeInstance()
                    .format(Date(BuildConfig.BUILD_DATE.toLong()))
            )
    }

    companion object {

        private const val ARG_SERVER_URLS = "server_urls"

        /**
         * Use this factory method to create a new instance of [PreferencesFragment].
         *
         * @return A new instance of [PreferencesFragment]
         */
        @JvmStatic
        fun newInstance(serverUrls: IGeoNatureAPIClient.ServerUrls?) = PreferencesFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_SERVER_URLS,
                    serverUrls
                )
            }
        }
    }
}