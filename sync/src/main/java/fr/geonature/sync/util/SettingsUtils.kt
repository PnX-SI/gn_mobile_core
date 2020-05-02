package fr.geonature.sync.util

import android.content.Context
import android.os.Environment
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import fr.geonature.mountpoint.util.MountPointUtils.getExternalStorage
import fr.geonature.mountpoint.util.MountPointUtils.getInternalStorage
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
                context.getString(R.string.preference_category_server_geonature_url_key),
                null
            )
    }

    fun setGeoNatureServerUrl(context: Context, geoNatureServerUrl: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(
                context.getString(R.string.preference_category_server_geonature_url_key),
                geoNatureServerUrl
            )
            .apply()
    }

    /**
     * Gets the current TaxHub server url to use.
     *
     * @param context the current context
     *
     * @return the TaxHub server url to use or `null` if not defined
     */
    fun getTaxHubServerUrl(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(
                context.getString(R.string.preference_category_server_taxhub_url_key),
                null
            )
    }

    fun setTaxHubServerUrl(context: Context, taxHubServerUrl: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(
                context.getString(R.string.preference_category_server_taxhub_url_key),
                taxHubServerUrl
            )
            .apply()
    }

    fun updatePreferences(preferenceScreen: PreferenceScreen) {
        val context = preferenceScreen.context
        val onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                preference.summary = newValue.toString()
                true
            }

        preferenceScreen.findPreference<EditTextPreference?>(context.getString(R.string.preference_category_server_geonature_url_key))
            ?.apply {
                summary = getGeoNatureServerUrl(preferenceScreen.context)
                setOnPreferenceChangeListener(onPreferenceChangeListener)
            }
        preferenceScreen.findPreference<EditTextPreference?>(context.getString(R.string.preference_category_server_taxhub_url_key))
            ?.apply {
                summary = getTaxHubServerUrl(preferenceScreen.context)
                setOnPreferenceChangeListener(onPreferenceChangeListener)
            }

        preferenceScreen.findPreference<Preference?>(context.getString(R.string.preference_category_storage_internal_key))
            ?.summary = getInternalStorage(preferenceScreen.context).mountPath.absolutePath
        getExternalStorage(
            preferenceScreen.context,
            Environment.MEDIA_MOUNTED,
            Environment.MEDIA_MOUNTED_READ_ONLY
        )?.also { mountPoint ->
            preferenceScreen.findPreference<Preference?>(context.getString(R.string.preference_category_storage_external_key))
                ?.also {
                    it.summary = mountPoint.mountPath.absolutePath
                    it.isEnabled = true
                }
        }

        preferenceScreen.findPreference<Preference?>(context.getString(R.string.preference_category_about_app_version_key))
            ?.summary = context.getString(
            R.string.app_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            DateFormat.getDateTimeInstance()
                .format(Date(BuildConfig.BUILD_DATE.toLong()))
        )
    }
}
