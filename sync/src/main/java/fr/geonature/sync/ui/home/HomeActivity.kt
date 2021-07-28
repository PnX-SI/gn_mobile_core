package fr.geonature.sync.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.util.observeOnce
import fr.geonature.commons.util.observeUntil
import fr.geonature.sync.BuildConfig
import fr.geonature.sync.MainApplication
import fr.geonature.sync.R
import fr.geonature.sync.auth.AuthLoginViewModel
import fr.geonature.sync.settings.AppSettings
import fr.geonature.sync.settings.AppSettingsViewModel
import fr.geonature.sync.sync.DataSyncViewModel
import fr.geonature.sync.sync.PackageInfo
import fr.geonature.sync.sync.PackageInfoViewModel
import fr.geonature.sync.sync.ServerStatus.UNAUTHORIZED
import fr.geonature.sync.ui.login.LoginActivity
import fr.geonature.sync.ui.settings.PreferencesActivity
import fr.geonature.sync.util.SettingsUtils.getGeoNatureServerUrl
import fr.geonature.sync.util.SettingsUtils.getTaxHubServerUrl
import fr.geonature.sync.util.SettingsUtils.setGeoNatureServerUrl
import fr.geonature.sync.util.SettingsUtils.setTaxHubServerUrl
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.ExperimentalTime

/**
 * Home screen Activity.
 *
 * @author S. Grimault
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

    private var progressSnackbar: Pair<Snackbar, CircularProgressIndicator>? = null

    private var appSettings: AppSettings? = null
    private var isLoggedIn: Boolean = false

    private lateinit var startSyncResultLauncher: ActivityResultLauncher<Intent>

    @ExperimentalTime
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
        packageInfoViewModel = configurePackageInfoViewModel()
        dataSyncViewModel = configureDataSyncViewModel()

        adapter = PackageInfoRecyclerViewAdapter(object :
            PackageInfoRecyclerViewAdapter.OnPackageInfoRecyclerViewAdapterListener {
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

            override fun onUpgrade(item: PackageInfo) {
                packageInfoViewModel.cancelTasks()
                downloadApk(item.packageName)
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

        startSyncResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> {
                    val appSettings = appSettings

                    if (appSettings == null) {
                        packageInfoViewModel.getAllApplications()
                    } else {
                        dataSyncViewModel.startSync(appSettings)
                        synchronizeInstalledApplications()
                    }
                }
            }
        }

        checkNetwork()
        loadAppSettings {
            packageInfoViewModel.getAllApplications()
        }
    }

    override fun onResume() {
        super.onResume()

        if (appSettings != null) {
            synchronizeInstalledApplications()
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
        menu?.run {
            findItem(R.id.menu_sync_data_refresh)?.also {
                it.isEnabled = isLoggedIn && appSettings != null && dataSyncViewModel.isSyncRunning.value != true
            }
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
                startSyncResultLauncher.launch(PreferencesActivity.newIntent(this))
                true
            }
            R.id.menu_sync_data_refresh -> {
                appSettings?.run {
                    dataSyncViewModel.startSync(this)
                }
                true
            }
            R.id.menu_login -> {
                startSyncResultLauncher.launch(LoginActivity.newIntent(this))
                true
            }
            R.id.menu_logout -> {
                authLoginViewModel
                    .logout()
                    .observe(this,
                        {
                            Toast
                                .makeText(
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
            AuthLoginViewModel.Factory {
                AuthLoginViewModel(
                    application,
                    (application as MainApplication).sl.authManager,
                    (application as MainApplication).sl.geoNatureAPIClient
                )
            })
            .get(AuthLoginViewModel::class.java)
            .also { vm ->
                vm
                    .checkAuthLogin()
                    .observeOnce(this@HomeActivity) {
                        if (checkGeoNatureSettings() && it == null) {
                            Log.i(
                                TAG,
                                "not connected, redirect to LoginActivity"
                            )

                            startSyncResultLauncher.launch(LoginActivity.newIntent(this@HomeActivity))
                        }
                    }
                vm.isLoggedIn.observe(this@HomeActivity,
                    {
                        this@HomeActivity.isLoggedIn = it
                        invalidateOptionsMenu()
                    })
            }
    }

    @ExperimentalTime
    private fun configurePackageInfoViewModel(): PackageInfoViewModel {
        return ViewModelProvider(this,
            PackageInfoViewModel.Factory {
                PackageInfoViewModel(
                    application,
                    (application as MainApplication).sl.packageInfoManager
                )
            })
            .get(
                PackageInfoViewModel::class.java
            )
            .also { vm ->
                vm.updateAvailable.observeOnce(this@HomeActivity) { appPackage ->
                    appPackage?.run { confirmBeforeUpgrade(this.packageName) }
                }

                vm.appSettingsUpdated.observeOnce(this@HomeActivity) {
                    Log.d(
                        TAG,
                        "reloading settings after update..."
                    )

                    loadAppSettings {
                        makeSnackbar(
                            getString(
                                R.string.snackbar_settings_updated,
                                appSettingsViewModel.getAppSettingsFilename()
                            )
                        )?.show()

                        startFirstSync(it)
                        synchronizeInstalledApplications()
                    }
                }

                vm.packageInfos.observe(this@HomeActivity,
                    {
                        progressBar?.visibility = View.GONE
                        adapter.setItems(it)
                    })
            }
    }

    private fun configureDataSyncViewModel(): DataSyncViewModel {
        return ViewModelProvider(this,
            DataSyncViewModel.Factory { DataSyncViewModel(application) })
            .get(DataSyncViewModel::class.java)
            .also { vm ->
                vm.lastSynchronizedDate.observe(this@HomeActivity,
                    {
                        dataSyncView?.setLastSynchronizedDate(it)
                    })
                vm.isSyncRunning.observe(this@HomeActivity,
                    {
                        invalidateOptionsMenu()
                    })
                vm
                    .observeDataSyncStatus()
                    .observe(this@HomeActivity,
                        {
                            if (it == null) {
                                dataSyncView?.setState(WorkInfo.State.ENQUEUED)
                                dataSyncView?.setMessage(null)
                            }

                            it?.run {
                                dataSyncView?.setState(if (it.syncMessage.isNullOrBlank()) WorkInfo.State.ENQUEUED else it.state)
                                dataSyncView?.setMessage(it.syncMessage)

                                if (it.serverStatus == UNAUTHORIZED) {
                                    Log.i(
                                        TAG,
                                        "not connected (HTTP error code: 401), redirect to LoginActivity"
                                    )

                                    Toast
                                        .makeText(
                                            this@HomeActivity,
                                            R.string.toast_not_connected,
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()

                                    startSyncResultLauncher.launch(LoginActivity.newIntent(this@HomeActivity))
                                }
                            }
                        })
            }
    }

    @RequiresPermission(Manifest.permission.CHANGE_NETWORK_STATE)
    private fun checkNetwork() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (connectivityManager.allNetworks.isEmpty()) {
            makeSnackbar(getString(R.string.snackbar_network_lost_sync))?.show()
            return
        }

        connectivityManager.requestNetwork(NetworkRequest
            .Builder()
            .build(),
            object : ConnectivityManager.NetworkCallback() {

                override fun onLost(network: Network) {
                    makeSnackbar(getString(R.string.snackbar_network_lost_sync))?.show()
                }

                override fun onUnavailable() {
                    makeSnackbar(getString(R.string.snackbar_network_lost_sync))?.show()
                }
            })
    }

    @ExperimentalTime
    private fun loadAppSettings(appSettingsLoaded: ((appSettings: AppSettings) -> Unit)? = null) {
        appSettingsViewModel
            .loadAppSettings()
            .observeOnce(this@HomeActivity) {
                if (it == null) {
                    makeSnackbar(
                        getString(
                            R.string.snackbar_settings_not_found,
                            appSettingsViewModel.getAppSettingsFilename()
                        )
                    )?.show()

                    progressBar?.visibility = View.GONE

                    if (!checkGeoNatureSettings()) {
                        startSyncResultLauncher.launch(PreferencesActivity.newIntent(this))
                    }

                    return@observeOnce
                }

                appSettings = it
                mergeAppSettingsWithSharedPreferences(it)
                invalidateOptionsMenu()

                if (!checkGeoNatureSettings()) {
                    startSyncResultLauncher.launch(PreferencesActivity.newIntent(this))

                    return@observeOnce
                }

                dataSyncViewModel.configurePeriodicSync(it)
                appSettingsLoaded?.invoke(it)
            }
    }

    private fun checkGeoNatureSettings(): Boolean {
        return (application as MainApplication).sl.geoNatureAPIClient.checkSettings()
    }

    private fun startFirstSync(appSettings: AppSettings) {
        if (dataSyncViewModel.lastSynchronizedDate.value == null && dataSyncViewModel.isSyncRunning.value != true) {
            dataSyncViewModel.startSync(appSettings)
        }
    }

    private fun synchronizeInstalledApplications() {
        GlobalScope.launch(Main) {
            progressBar?.visibility = View.VISIBLE

            delay(500)

            packageInfoViewModel.synchronizeInstalledApplications()
        }
    }

    private fun makeSnackbar(
        text: CharSequence,
        @BaseTransientBottomBar.Duration duration: Int = Snackbar.LENGTH_LONG
    ): Snackbar? {
        val view = homeContent
            ?: return null

        return Snackbar.make(
            view,
            text,
            duration
        )
    }

    private fun makeProgressSnackbar(text: CharSequence): Pair<Snackbar, CircularProgressIndicator>? {
        val view = homeContent
            ?: return null

        return Snackbar
            .make(
                view,
                text,
                Snackbar.LENGTH_INDEFINITE
            )
            .let { snackbar ->
                val circularProgressIndicator = CircularProgressIndicator(this).also {
                    it.isIndeterminate = true
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                (snackbar.view.findViewById<View>(com.google.android.material.R.id.snackbar_text).parent as ViewGroup).addView(
                    circularProgressIndicator,
                    0
                )

                Pair(
                    snackbar,
                    circularProgressIndicator
                )
            }
    }

    private fun mergeAppSettingsWithSharedPreferences(appSettings: AppSettings) {
        val geoNatureServerUrl = appSettings.geoNatureServerUrl
        val taxHubServerUrl = appSettings.taxHubServerUrl

        if (!geoNatureServerUrl.isNullOrBlank() && getGeoNatureServerUrl(this).isNullOrBlank()) {
            setGeoNatureServerUrl(
                this,
                geoNatureServerUrl
            )
        }

        if (!taxHubServerUrl.isNullOrBlank() && getTaxHubServerUrl(this).isNullOrBlank()) {
            setTaxHubServerUrl(
                this,
                taxHubServerUrl
            )
        }
    }

    private fun confirmBeforeUpgrade(packageName: String) {
        AlertDialog
            .Builder(this)
            .setIcon(R.drawable.ic_upgrade)
            .setTitle(R.string.alert_new_app_version_available_title)
            .setMessage(R.string.alert_new_app_version_available_description)
            .setPositiveButton(
                R.string.alert_new_app_version_action_ok
            ) { dialog, _ ->
                dataSyncViewModel.cancelTasks()
                packageInfoViewModel.cancelTasks()
                downloadApk(packageName)
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.alert_new_app_version_action_later
            ) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun downloadApk(packageName: String) {
        if (packageName == BuildConfig.APPLICATION_ID) {
            progressSnackbar = makeProgressSnackbar(getString(R.string.snackbar_upgrading_app))?.also { it.first.show() }
        }

        packageInfoViewModel
            .downloadAppPackage(packageName)
            .observeUntil(this@HomeActivity,
                { appPackageDownloadStatus ->
                    appPackageDownloadStatus?.state in arrayListOf(
                        WorkInfo.State.SUCCEEDED,
                        WorkInfo.State.FAILED,
                        WorkInfo.State.CANCELLED
                    )
                }) {
                it?.run {
                    when (state) {
                        WorkInfo.State.FAILED -> {
                            if (packageName == BuildConfig.APPLICATION_ID) progressSnackbar?.first?.dismiss()
                        }
                        WorkInfo.State.SUCCEEDED -> {
                            if (packageName == BuildConfig.APPLICATION_ID) progressSnackbar?.first?.dismiss()
                            apkFilePath?.run {
                                installApk(this)
                            }
                        }
                        else -> {
                            if (packageName == BuildConfig.APPLICATION_ID) {
                                progressSnackbar?.second?.also { circularProgressIndicator ->
                                    circularProgressIndicator.isIndeterminate = false
                                    circularProgressIndicator.progress = progress
                                }
                            }
                        }
                    }
                }
            }
    }

    private fun installApk(apkFilePath: String) {
        val contentUri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.file.provider",
            File(apkFilePath)
        )
        val install = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(
                Intent.EXTRA_NOT_UNKNOWN_SOURCE,
                true
            )
            data = contentUri
        }

        startActivity(install)
    }

    companion object {
        private val TAG = HomeActivity::class.java.name
    }
}
