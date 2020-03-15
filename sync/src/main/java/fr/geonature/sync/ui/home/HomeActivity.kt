package fr.geonature.sync.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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
import fr.geonature.sync.sync.ServerStatus
import fr.geonature.sync.ui.login.LoginActivity
import fr.geonature.sync.ui.settings.PreferencesActivity
import fr.geonature.sync.util.observeOnce
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

    private lateinit var authLoginViewModel: AuthLoginViewModel
    private lateinit var dataSyncViewModel: DataSyncViewModel
    private lateinit var packageInfoViewModel: PackageInfoViewModel
    private lateinit var adapter: PackageInfoRecyclerViewAdapter
    private var appSettings: AppSettings? = null
    private var isLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        authLoginViewModel = configureAuthLoginViewModel()
        dataSyncViewModel = configureDataSyncViewModel()
        packageInfoViewModel = configurePackageInfoViewModel()

        adapter = PackageInfoRecyclerViewAdapter(object :
            AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<PackageInfo> {
            override fun onClick(item: PackageInfo) {
                item.launchIntent?.run {
                    startActivity(this)
                }
            }

            override fun onLongClicked(
                position: Int,
                item: PackageInfo
            ) {
                // nothing to do...
            }

            override fun showEmptyTextView(show: Boolean) {
                if (emptyTextView.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    emptyTextView.startAnimation(
                        loadAnimation(
                            this@HomeActivity,
                            android.R.anim.fade_in
                        )
                    )
                    emptyTextView.visibility = View.VISIBLE
                } else {
                    emptyTextView.startAnimation(
                        loadAnimation(
                            this@HomeActivity,
                            android.R.anim.fade_out
                        )
                    )
                    emptyTextView.visibility = View.GONE
                }
            }
        })

        with(appRecyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeActivity.adapter

            val dividerItemDecoration = DividerItemDecoration(
                context,
                (layoutManager as LinearLayoutManager).orientation
            )
            addItemDecoration(dividerItemDecoration)
        }

        loadAppSettings()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (resultCode == Activity.RESULT_OK) {
            startSync()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(
            R.menu.settings,
            menu
        )

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
                            Toast.makeText(
                                    this,
                                    R.string.toast_logout_success,
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        })
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun configureAuthLoginViewModel(): AuthLoginViewModel {
        return ViewModelProvider(this,
            AuthLoginViewModel.Factory { AuthLoginViewModel(application) }).get(AuthLoginViewModel::class.java)
            .also { vm ->
                vm.isLoggedIn.observe(this@HomeActivity,
                    Observer {
                        this@HomeActivity.isLoggedIn = it
                        invalidateOptionsMenu()

                    })
            }
    }

    private fun configureDataSyncViewModel(): DataSyncViewModel {
        return ViewModelProvider(this,
            DataSyncViewModel.Factory { DataSyncViewModel(application) }).get(DataSyncViewModel::class.java)
            .also { vm ->
                vm.syncOutputStatus.takeIf { !it.hasActiveObservers() }
                    ?.observe(this,
                        Observer {
                            if (it == null || it.isEmpty()) {
                                return@Observer
                            }

                            val workInfo = it[0]
                            dataSyncView.setState(workInfo.state)
                        })
                vm.lastSynchronizedDate.takeIf { !it.hasActiveObservers() }
                    ?.observe(this,
                        Observer {
                            dataSyncView.setLastSynchronizedDate(it)
                        })
                vm.syncMessage.takeIf { !it.hasActiveObservers() }
                    ?.observe(this,
                        Observer {
                            dataSyncView.setMessage(it)
                        })
                vm.serverStatus.takeIf { !it.hasActiveObservers() }
                    ?.observe(this,
                        Observer {
                            if (it == null) return@Observer

                            when (it) {
                                ServerStatus.INTERNAL_SERVER_ERROR -> packageInfoViewModel.cancelTasks()
                                ServerStatus.FORBIDDEN -> {
                                    packageInfoViewModel.cancelTasks()

                                    Toast.makeText(
                                            this,
                                            R.string.toast_not_connected,
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()

                                    if (appSettings != null) {
                                        startActivityForResult(
                                            LoginActivity.newIntent(this),
                                            0
                                        )
                                    }
                                }
                            }
                        })
            }
    }

    private fun configurePackageInfoViewModel(): PackageInfoViewModel {
        return ViewModelProvider(this,
            PackageInfoViewModel.Factory { PackageInfoViewModel(application) }).get(
                PackageInfoViewModel::class.java
            )
            .also { vm ->
                vm.packageInfos.observe(this@HomeActivity,
                    Observer {
                        progress.visibility = View.GONE
                        adapter.setItems(it)
                    })
            }
    }

    private fun loadAppSettings() {
        ViewModelProvider(this,
            fr.geonature.commons.settings.AppSettingsViewModel.Factory {
                AppSettingsViewModel(
                    application
                )
            }).get(AppSettingsViewModel::class.java)
            .also { vm ->
                vm.getAppSettings<AppSettings>()
                    .observeOnce(this) {
                        if (it == null) {
                            Snackbar.make(
                                    homeContent,
                                    getString(
                                        R.string.snackbar_settings_not_found,
                                        vm.getAppSettingsFilename()
                                    ),
                                    Snackbar.LENGTH_LONG
                                )
                                .show()
                        } else {
                            appSettings = it

                            startSync()
                        }
                    }
            }
    }

    private fun startSync() {
        val appSettings = appSettings ?: return

        GlobalScope.launch(Main) {
            delay(250)
            dataSyncViewModel.startSync(appSettings)

            progress.visibility = View.VISIBLE
            delay(500)
            packageInfoViewModel.getInstalledApplications()
        }
    }
}
