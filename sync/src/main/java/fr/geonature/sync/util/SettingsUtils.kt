package fr.geonature.sync.util

import android.content.Context
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import fr.geonature.sync.BuildConfig
import fr.geonature.sync.R
import java.text.DateFormat
import java.util.Date

/**
 * Helper about application settings through [Preference].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object SettingsUtils {

    /**
     * Gets the current GeoNature server url to use.
     *
     * @param context the current context
     *
     * @return the GeoNature server url to use or `null` if not defined
     */
    fun getGeoNatureServerUrl(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(
                context.getString(R.string.preference_category_server_url_key),
                null
            )
    }

    fun updatePreferences(preferenceScreen: PreferenceScreen) {
        val context = preferenceScreen.context
        val onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString()
                true
            }

        preferenceScreen.findPreference<EditTextPreference?>(context.getString(R.string.preference_category_server_url_key))
            ?.apply {
                summary = getGeoNatureServerUrl(preferenceScreen.context)
                setOnPreferenceChangeListener(onPreferenceChangeListener)
            }

        preferenceScreen.findPreference<Preference?>(context.getString(R.string.preference_category_about_app_version_key))
            ?.summary = context.getString(
            R.string.app_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            DateFormat.getDateTimeInstance().format(Date(BuildConfig.BUILD_DATE.toLong()))
        )
    }
}
