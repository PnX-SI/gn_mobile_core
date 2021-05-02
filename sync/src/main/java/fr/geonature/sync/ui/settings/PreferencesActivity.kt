package fr.geonature.sync.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.sync.util.SettingsUtils.getGeoNatureServerUrl
import fr.geonature.sync.util.SettingsUtils.getTaxHubServerUrl

/**
 * Global settings.
 *
 * @see PreferencesFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PreferencesActivity : AppCompatActivity() {

    private var geonatureServerUrl: String? = null
    private var taxhubServerUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        geonatureServerUrl = getGeoNatureServerUrl(this)
        taxhubServerUrl = getTaxHubServerUrl(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Display the fragment as the main content.
        supportFragmentManager
            .beginTransaction()
            .replace(
                android.R.id.content,
                PreferencesFragment.newInstance()
            )
            .commit()
    }

    override fun finish() {
        setResult(
            if (geonatureServerUrl != getGeoNatureServerUrl(this) || taxhubServerUrl != getTaxHubServerUrl(this)) RESULT_OK
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
