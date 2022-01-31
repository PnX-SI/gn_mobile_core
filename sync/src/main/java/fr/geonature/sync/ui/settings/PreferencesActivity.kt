package fr.geonature.sync.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.commons.fp.getOrElse
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.datasync.settings.DataSyncSettingsViewModel

/**
 * Global settings.
 *
 * @see PreferencesFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@AndroidEntryPoint
class PreferencesActivity : AppCompatActivity() {

    private val dataSyncSettingsViewModel: DataSyncSettingsViewModel by viewModels()

    private var serverUrls: IGeoNatureAPIClient.ServerUrls? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serverUrls = dataSyncSettingsViewModel
            .getServerBaseUrls()
            .getOrElse(null)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Display the fragment as the main content.
        supportFragmentManager
            .beginTransaction()
            .replace(
                android.R.id.content,
                PreferencesFragment.newInstance(serverUrls)
            )
            .commit()
    }

    override fun finish() {
        val currentServerUrls = dataSyncSettingsViewModel
            .getServerBaseUrls()
            .getOrElse(null)

        if (currentServerUrls != null) {
            dataSyncSettingsViewModel.setServerBaseUrls(
                geoNatureServerUrl = currentServerUrls.geoNatureBaseUrl,
                taxHubServerUrl = currentServerUrls.taxHubBaseUrl
            )
        }

        setResult(
            if (serverUrls != currentServerUrls) RESULT_OK
            else RESULT_CANCELED
        )

        super.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(
                context,
                PreferencesActivity::class.java
            )
        }
    }
}
