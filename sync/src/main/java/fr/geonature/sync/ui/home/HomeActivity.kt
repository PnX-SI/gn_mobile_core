package fr.geonature.sync.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import fr.geonature.sync.R
import fr.geonature.sync.sync.DataSyncViewModel
import fr.geonature.sync.sync.PackageInfo
import fr.geonature.sync.sync.PackageInfoViewModel
import fr.geonature.sync.ui.settings.PreferencesActivity

/**
 * Home screen Activity.
 *
 * @see HomeFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class HomeActivity : AppCompatActivity(),
                     HomeFragment.OnHomeFragmentListener {

    private lateinit var dataSyncViewModel: DataSyncViewModel
    private lateinit var packageInfoViewModel: PackageInfoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataSyncViewModel = ViewModelProvider(this,
                                              DataSyncViewModel.Factory { DataSyncViewModel(this.application) }).get(DataSyncViewModel::class.java)
        packageInfoViewModel = ViewModelProvider(this,
                                                 PackageInfoViewModel.Factory { PackageInfoViewModel(this.application) }).get(PackageInfoViewModel::class.java)

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content,
                         HomeFragment.newInstance())
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings,
                             menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_settings -> {
                startActivity(PreferencesActivity.newIntent(this))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClicked(packageInfo: PackageInfo) {
        packageInfo.launchIntent?.run {
            startActivity(this)
        }
    }
}
