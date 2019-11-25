package fr.geonature.sync.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.sync.R
import fr.geonature.sync.auth.AuthLoginViewModel
import fr.geonature.sync.settings.AppSettings
import fr.geonature.sync.settings.AppSettingsViewModel
import fr.geonature.sync.sync.DataSyncViewModel
import fr.geonature.sync.sync.PackageInfo
import fr.geonature.sync.sync.PackageInfoViewModel
import fr.geonature.sync.ui.login.LoginActivity
import fr.geonature.sync.ui.settings.PreferencesActivity
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Home screen Activity.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var appSettingsViewModel: AppSettingsViewModel
    private lateinit var authLoginViewModel: AuthLoginViewModel
    private lateinit var dataSyncViewModel: DataSyncViewModel
    private lateinit var packageInfoViewModel: PackageInfoViewModel
    private lateinit var adapter: PackageInfoRecyclerViewAdapter
    private var appSettings: AppSettings? = null
    private var isLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        appSettingsViewModel = ViewModelProvider(this,
                                                 fr.geonature.commons.settings.AppSettingsViewModel.Factory { AppSettingsViewModel(application) }).get(AppSettingsViewModel::class.java)
        authLoginViewModel = ViewModelProvider(this,
                                               AuthLoginViewModel.Factory { AuthLoginViewModel(application) }).get(AuthLoginViewModel::class.java)
                .apply {
                    isLoggedIn.observe(this@HomeActivity,
                                       Observer {
                                           this@HomeActivity.isLoggedIn = it
                                           invalidateOptionsMenu()
                                       })
                }
        dataSyncViewModel = ViewModelProvider(this,
                                              DataSyncViewModel.Factory { DataSyncViewModel(application) }).get(DataSyncViewModel::class.java)
        packageInfoViewModel = ViewModelProvider(this,
                                                 PackageInfoViewModel.Factory { PackageInfoViewModel(application) }).get(PackageInfoViewModel::class.java)

        adapter = PackageInfoRecyclerViewAdapter(object : AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<PackageInfo> {
            override fun onClick(item: PackageInfo) {
                item.launchIntent?.run {
                    startActivity(this)
                }
            }

            override fun onLongClicked(position: Int,
                                       item: PackageInfo) {
                // nothing to do...
            }

            override fun showEmptyTextView(show: Boolean) {
                if (emptyTextView.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    emptyTextView.startAnimation(loadAnimation(this@HomeActivity,
                                                               android.R.anim.fade_in))
                    emptyTextView.visibility = View.VISIBLE

                }
                else {
                    emptyTextView.startAnimation(loadAnimation(this@HomeActivity,
                                                               android.R.anim.fade_out))
                    emptyTextView.visibility = View.GONE
                }
            }
        })

        with(appRecyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeActivity.adapter

            val dividerItemDecoration = DividerItemDecoration(context,
                                                              (layoutManager as LinearLayoutManager).orientation)
            addItemDecoration(dividerItemDecoration)
        }

        loadAppSettings()
        startSync()
        getInstalledApplications()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings,
                             menu)

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.menu_login)
                ?.isVisible = !isLoggedIn
        menu?.findItem(R.id.menu_logout)
                ?.isVisible = isLoggedIn

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_settings -> {
                startActivity(PreferencesActivity.newIntent(this))
                true
            }
            R.id.menu_login -> {
                startActivity(LoginActivity.newIntent(this))
                true
            }
            R.id.menu_logout -> {
                authLoginViewModel.logout()
                        .observe(this,
                                 Observer {
                                     Toast.makeText(this,
                                                    R.string.logout_success,
                                                    Toast.LENGTH_SHORT)
                                             .show()
                                 })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startSync() {
        dataSyncViewModel.syncOutputStatus.takeIf { !it.hasActiveObservers() }
                ?.observe(this,
                          Observer {
                              if (it == null || it.isEmpty()) {
                                  return@Observer
                              }

                              val workInfo = it[0]
                              dataSyncView.setState(workInfo.state)
                          })
        dataSyncViewModel.lastSynchronizedDate.takeIf { !it.hasActiveObservers() }
                ?.observe(this,
                          Observer {
                              dataSyncView.setLastSynchronizedDate(it)
                          })
        dataSyncViewModel.syncMessage.takeIf { !it.hasActiveObservers() }
                ?.observe(this,
                          Observer {
                              dataSyncView.setMessage(it)
                          })

        dataSyncViewModel.startSync()
    }

    private fun getInstalledApplications() {
        GlobalScope.launch(Main) {
            progress.visibility = View.VISIBLE

            delay(500)

            packageInfoViewModel.getInstalledApplications()
            packageInfoViewModel.packageInfos.observe(this@HomeActivity,
                                                      Observer {
                                                          progress.visibility = View.GONE
                                                          adapter.setItems(it)
                                                      })
        }
    }

    private fun loadAppSettings() {
        appSettingsViewModel.getAppSettings<AppSettings>()
                .observe(this,
                         Observer {
                             if (it == null) {
                                 Snackbar.make(homeContent,
                                               getString(R.string.snackbar_settings_not_found,
                                                         appSettingsViewModel.getAppSettingsFilename()),
                                               Snackbar.LENGTH_LONG)
                                         .show()
                             }
                             else {
                                 appSettings = it
                             }
                         })
    }
}
