package fr.geonature.sync.ui.home

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.ui.adapter.AbstractListItemRecyclerViewAdapter
import fr.geonature.sync.BuildConfig
import fr.geonature.sync.R
import fr.geonature.sync.api.GeoNatureAPIClient
import fr.geonature.sync.api.model.AppPackage
import fr.geonature.sync.auth.AuthLoginViewModel
import fr.geonature.sync.settings.AppSettings
import fr.geonature.sync.settings.AppSettingsViewModel
import fr.geonature.sync.sync.DataSyncViewModel
import fr.geonature.sync.sync.PackageInfo
import fr.geonature.sync.sync.PackageInfoViewModel
import fr.geonature.sync.sync.ServerStatus.FORBIDDEN
import fr.geonature.sync.sync.ServerStatus.INTERNAL_SERVER_ERROR
import fr.geonature.sync.ui.login.LoginActivity
import fr.geonature.sync.ui.settings.PreferencesActivity
import fr.geonature.sync.util.SettingsUtils.getGeoNatureServerUrl
import fr.geonature.sync.util.SettingsUtils.getTaxHubServerUrl
import fr.geonature.sync.util.SettingsUtils.setGeoNatureServerUrl
import fr.geonature.sync.util.SettingsUtils.setTaxHubServerUrl
import fr.geonature.sync.util.observeOnce
import fr.geonature.sync.util.observeUntil
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

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

    private var homeContent: ConstraintLayout? = null
    private var emptyTextView: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var dataSyncView: DataSyncView? = null

    @Suppress("DEPRECATION")
    private var progressDialog: ProgressDialog? = null

    private var appSettings: AppSettings? = null
    private var isLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        homeContent = findViewById(R.id.homeContent)
        emptyTextView = findViewById(R.id.emptyTextView)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(android.R.id.progress)
        dataSyncView = findViewById(R.id.dataSyncView)

        appSettingsViewModel = configureAppSettingsViewModel()
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
                if (emptyTextView?.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    emptyTextView?.startAnimation(
                        loadAnimation(
                            this@HomeActivity,
                            android.R.anim.fade_in
                        )
                    )
                    emptyTextView?.visibility = View.VISIBLE
                } else {
                    emptyTextView?.startAnimation(
                        loadAnimation(
                            this@HomeActivity,
                            android.R.anim.fade_out
                        )
                    )
                    emptyTextView?.visibility = View.GONE
                }
            }
        })

        with(recyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeActivity.adapter

            val dividerItemDecoration = DividerItemDecoration(
                context,
                (layoutManager as LinearLayoutManager).orientation
            )
            addItemDecoration(dividerItemDecoration)
        }
    }

    override fun onResume() {
        super.onResume()

        loadAppSettings()
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
        menu?.run {
            findItem(R.id.menu_login)?.also {
                it.isEnabled = appSettings != null
                it.isVisible = !isLoggedIn
            }
            findItem(R.id.menu_logout)?.also {
                it.isEnabled = appSettings != null
                it.isVisible = isLoggedIn
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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

    private fun configureAppSettingsViewModel(): AppSettingsViewModel {
        return ViewModelProvider(this,
            fr.geonature.commons.settings.AppSettingsViewModel.Factory {
                AppSettingsViewModel(application)
            }).get(AppSettingsViewModel::class.java)
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
    }

    private fun configurePackageInfoViewModel(): PackageInfoViewModel {
        return ViewModelProvider(this,
            PackageInfoViewModel.Factory { PackageInfoViewModel(application) }).get(
            PackageInfoViewModel::class.java
        )
            .also { vm ->
                vm.updateAvailable.observeUntil(
                    this@HomeActivity,
                    { appPackage -> appPackage != null }) { appPackage ->
                    appPackage?.run { confirmBeforeUpgrade(this) }
                }

                vm.packageInfos.observe(this@HomeActivity,
                    Observer {
                        progressBar?.visibility = View.GONE
                        adapter.setItems(it)
                    })

                vm.appPackageDownloadStatus.observe(
                    this@HomeActivity,
                    Observer {
                        it?.run {
                            when (state) {
                                WorkInfo.State.FAILED -> progressDialog?.dismiss()
                                WorkInfo.State.SUCCEEDED -> {
                                    progressDialog?.dismiss()
                                    apkFilePath?.run {
                                        installApk(this)
                                    }
                                }
                                else -> showProgressDialog(progress)
                            }
                        }
                    })
            }
    }

    private fun observeDataSyncStatus(dataSyncViewModel: DataSyncViewModel) {
        dataSyncViewModel.dataSyncStatus.takeUnless { it.hasActiveObservers() }
            ?.observe(this,
                Observer {
                    if (it == null) {
                        return@Observer
                    }

                    dataSyncView?.setState(if (it.syncMessage.isNullOrBlank()) WorkInfo.State.ENQUEUED else it.state)

                    if (!it.syncMessage.isNullOrBlank()) {
                        dataSyncView?.setMessage(it.syncMessage)
                    }

                    @Suppress("NON_EXHAUSTIVE_WHEN")
                    when (it.serverStatus) {
                        INTERNAL_SERVER_ERROR -> packageInfoViewModel.cancelTasks()
                        FORBIDDEN -> {
                            packageInfoViewModel.cancelTasks()

                            Toast.makeText(
                                this,
                                R.string.toast_not_connected,
                                Toast.LENGTH_SHORT
                            )
                                .show()

                            if (appSettings != null) {
                                startActivity(LoginActivity.newIntent(this))
                            }
                        }
                    }
                })
        dataSyncViewModel.lastSynchronizedDate.takeUnless { it.hasActiveObservers() }
            ?.observe(this,
                Observer {
                    dataSyncView?.setLastSynchronizedDate(it)
                })
    }

    private fun loadAppSettings() {
        progressBar?.visibility = View.VISIBLE
        appSettingsViewModel.getAppSettings<AppSettings>()
            .observeOnce(this) {
                if (it == null) {
                    makeSnackbar(
                        getString(
                            R.string.snackbar_settings_not_found,
                            appSettingsViewModel.getAppSettingsFilename()
                        )
                    )?.show()

                    progressBar?.visibility = View.GONE
                    adapter.clear()

                    if (!checkGeoNatureSettings()) {
                        startActivity(PreferencesActivity.newIntent(this))
                        return@observeOnce
                    }

                    packageInfoViewModel.checkAppPackages()
                } else {
                    appSettings = it
                    mergeAppSettingsWithSharedPreferences(it)
                    invalidateOptionsMenu()

                    if (!checkGeoNatureSettings()) {
                        startActivity(PreferencesActivity.newIntent(this))
                        return@observeOnce
                    }

                    packageInfoViewModel.checkAppPackages()
                    startSync(it)
                }
            }
    }

    private fun checkGeoNatureSettings(): Boolean {
        return GeoNatureAPIClient.instance(this) != null
    }

    private fun startSync(appSettings: AppSettings) {
        GlobalScope.launch(Main) {
            delay(250)
            observeDataSyncStatus(dataSyncViewModel)
            dataSyncViewModel.startSync(appSettings)

            delay(500)
            packageInfoViewModel.getInstalledApplicationsToSynchronize()
        }
    }

    private fun makeSnackbar(text: CharSequence): Snackbar? {
        val view = homeContent ?: return null

        return Snackbar.make(
            view,
            text,
            Snackbar.LENGTH_LONG
        )
    }

    private fun mergeAppSettingsWithSharedPreferences(appSettings: AppSettings) {
        val geoNatureServerUrl = appSettings.geoNatureServerUrl

        if (!geoNatureServerUrl.isNullOrBlank() && getGeoNatureServerUrl(this).isNullOrBlank()) {
            setGeoNatureServerUrl(
                this,
                geoNatureServerUrl
            )
        }

        val taxHubServerUrl = appSettings.taxHubServerUrl

        if (!taxHubServerUrl.isNullOrBlank() && getTaxHubServerUrl(this).isNullOrBlank()) {
            setTaxHubServerUrl(
                this,
                taxHubServerUrl
            )
        }
    }

    private fun confirmBeforeUpgrade(appPackage: AppPackage) {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_upgrade)
            .setTitle(R.string.alert_new_app_version_available_title)
            .setMessage(R.string.alert_new_app_version_available_description)
            .setPositiveButton(
                R.string.alert_new_app_version_action_ok
            ) { dialog, _ ->
                dataSyncViewModel.cancelTasks()
                packageInfoViewModel.cancelTasks()
                packageInfoViewModel.downloadAppPackage(appPackage)
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.alert_new_app_version_action_later
            ) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    @Suppress("DEPRECATION")
    private fun showProgressDialog(progress: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this).apply {
                setCancelable(false)
                setIcon(R.drawable.ic_upgrade)
                setTitle(R.string.alert_new_app_version_available_title)
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setProgressNumberFormat(null)
            }
            progressDialog?.show()
        }

        progressDialog?.progress = progress
    }

    private fun installApk(apkFilePath: String) {
        val contentUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.file.provider",
            File(apkFilePath)
        )
        val install = Intent(Intent.ACTION_VIEW)
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        install.putExtra(
            Intent.EXTRA_NOT_UNKNOWN_SOURCE,
            true
        )
        install.data = contentUri
        startActivity(install)
    }
}
