package fr.geonature.sync.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
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
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.material.snackbar.Snackbar
import fr.geonature.commons.util.PermissionUtils
import fr.geonature.commons.util.observeOnce
import fr.geonature.commons.util.observeUntil
import fr.geonature.sync.BuildConfig
import fr.geonature.sync.R
import fr.geonature.sync.api.GeoNatureAPIClient
import fr.geonature.sync.auth.AuthLoginViewModel
import fr.geonature.sync.settings.AppSettings
import fr.geonature.sync.settings.AppSettingsViewModel
import fr.geonature.sync.sync.DataSyncViewModel
import fr.geonature.sync.sync.PackageInfo
import fr.geonature.sync.sync.PackageInfoViewModel
import fr.geonature.sync.sync.ServerStatus.FORBIDDEN
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
        packageInfoViewModel = configurePackageInfoViewModel()
        dataSyncViewModel = configureDataSyncViewModel()

        adapter = PackageInfoRecyclerViewAdapter(
            object :
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
            }
        )

        with(recyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeActivity.adapter

            val dividerItemDecoration = DividerItemDecoration(
                context,
                (layoutManager as LinearLayoutManager).orientation
            )
            addItemDecoration(dividerItemDecoration)
        }

        checkNetwork()
        checkSelfPermissions()
    }

    override fun onResume() {
        super.onResume()

        if (appSettings != null) {
            packageInfoViewModel.synchronizeInstalledApplications()
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
                startActivityForResult(
                    PreferencesActivity.newIntent(this),
                    REQUEST_CODE_SYNC
                )
                true
            }
            R.id.menu_login -> {
                startActivityForResult(
                    LoginActivity.newIntent(this),
                    REQUEST_CODE_SYNC
                )
                true
            }
            R.id.menu_logout -> {
                authLoginViewModel.logout()
                    .observe(
                        this,
                        {
                            Toast.makeText(
                                this,
                                R.string.toast_logout_success,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSIONS -> {
                val requestPermissionsResult = PermissionUtils.checkPermissions(grantResults)

                if (requestPermissionsResult) {
                    makeSnackbar(getString(R.string.snackbar_permission_external_storage_available))?.show()
                    loadAppSettingsAndStartSync()
                    packageInfoViewModel.getAvailableApplications()
                } else {
                    makeSnackbar(getString(R.string.snackbar_permissions_not_granted))?.show()
                }
            }
            else -> super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if ((resultCode != Activity.RESULT_OK)) {
            return
        }

        if (requestCode == REQUEST_CODE_SYNC) {
            if (appSettings == null) {
                packageInfoViewModel.getAvailableApplications()
            } else {
                appSettings?.run {
                    startSync(this)
                }
            }
        }
    }

    private fun configureAppSettingsViewModel(): AppSettingsViewModel {
        return ViewModelProvider(
            this,
            fr.geonature.commons.settings.AppSettingsViewModel.Factory {
                AppSettingsViewModel(application)
            }
        ).get(AppSettingsViewModel::class.java)
    }

    private fun configureAuthLoginViewModel(): AuthLoginViewModel {
        return ViewModelProvider(
            this,
            AuthLoginViewModel.Factory { AuthLoginViewModel(application) }
        ).get(AuthLoginViewModel::class.java)
            .also { vm ->
                vm.isLoggedIn.observe(
                    this@HomeActivity,
                    {
                        this@HomeActivity.isLoggedIn = it
                        invalidateOptionsMenu()
                    }
                )
            }
    }

    private fun configurePackageInfoViewModel(): PackageInfoViewModel {
        return ViewModelProvider(
            this,
            PackageInfoViewModel.Factory { PackageInfoViewModel(application) }
        ).get(
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

                    loadAppSettingsAndStartSync(true)
                }

                vm.packageInfos.observe(
                    this@HomeActivity,
                    {
                        progressBar?.visibility = View.GONE
                        adapter.setItems(it)
                    }
                )
            }
    }

    private fun configureDataSyncViewModel(): DataSyncViewModel {
        return ViewModelProvider(
            this,
            DataSyncViewModel.Factory { DataSyncViewModel(application) }
        ).get(DataSyncViewModel::class.java)
            .also { vm ->
                vm.lastSynchronizedDate.observe(
                    this@HomeActivity,
                    {
                        dataSyncView?.setLastSynchronizedDate(it)
                    }
                )
            }
    }

    private fun checkSelfPermissions() {
        PermissionUtils.checkSelfPermissions(
            this@HomeActivity,
            object : PermissionUtils.OnCheckSelfPermissionListener {
                override fun onPermissionsGranted() {
                    loadAppSettingsAndStartSync()
                    packageInfoViewModel.getAvailableApplications()
                }

                override fun onRequestPermissions(vararg permissions: String) {
                    homeContent?.also {
                        PermissionUtils.requestPermissions(
                            this@HomeActivity,
                            it,
                            R.string.snackbar_permission_external_storage_rationale,
                            REQUEST_STORAGE_PERMISSIONS,
                            *permissions
                        )
                    }
                }
            },
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    @RequiresPermission(Manifest.permission.CHANGE_NETWORK_STATE)
    private fun checkNetwork() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (connectivityManager.allNetworks.isEmpty()) {
            makeSnackbar(getString(R.string.snackbar_network_lost_sync))?.show()
            return
        }

        connectivityManager.requestNetwork(
            NetworkRequest.Builder()
                .build(),
            object :
                ConnectivityManager.NetworkCallback() {

                override fun onLost(network: Network) {
                    makeSnackbar(getString(R.string.snackbar_network_lost_sync))?.show()
                }

                override fun onUnavailable() {
                    makeSnackbar(getString(R.string.snackbar_network_lost_sync))?.show()
                }
            }
        )
    }

    private fun loadAppSettingsAndStartSync(updated: Boolean = false) {
        appSettingsViewModel.loadAppSettings()
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
                        startActivityForResult(
                            PreferencesActivity.newIntent(this),
                            REQUEST_CODE_SYNC
                        )

                        return@observeOnce
                    }
                } else {
                    if (updated) {
                        makeSnackbar(
                            getString(
                                R.string.snackbar_settings_updated,
                                appSettingsViewModel.getAppSettingsFilename()
                            )
                        )?.show()
                    }

                    appSettings = it
                    mergeAppSettingsWithSharedPreferences(it)
                    invalidateOptionsMenu()

                    if (!checkGeoNatureSettings()) {
                        startActivityForResult(
                            PreferencesActivity.newIntent(this),
                            REQUEST_CODE_SYNC
                        )

                        return@observeOnce
                    }

                    startSync(it)
                }
            }
    }

    private fun checkGeoNatureSettings(): Boolean {
        return GeoNatureAPIClient.instance(this) != null
    }

    private fun startSync(appSettings: AppSettings) {
        GlobalScope.launch(Main) {
            progressBar?.visibility = View.VISIBLE

            delay(500)

            dataSyncViewModel.startSync(appSettings)
                .observeUntil(
                    this@HomeActivity,
                    {
                        it?.state in arrayListOf(
                            WorkInfo.State.SUCCEEDED,
                            WorkInfo.State.FAILED,
                            WorkInfo.State.CANCELLED
                        )
                    }
                ) {
                    it?.run {
                        dataSyncView?.setState(if (it.syncMessage.isNullOrBlank()) WorkInfo.State.ENQUEUED else it.state)

                        if (!it.syncMessage.isNullOrBlank()) {
                            dataSyncView?.setMessage(it.syncMessage)
                        }

                        if (it.serverStatus == FORBIDDEN) {
                            Log.d(
                                TAG,
                                "not connected, redirect to LoginActivity"
                            )

                            Toast.makeText(
                                this@HomeActivity,
                                R.string.toast_not_connected,
                                Toast.LENGTH_SHORT
                            )
                                .show()

                            startActivityForResult(
                                LoginActivity.newIntent(this@HomeActivity),
                                REQUEST_CODE_SYNC
                            )
                        }
                    }
                }

            delay(500)

            packageInfoViewModel.synchronizeInstalledApplications()
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
        AlertDialog.Builder(this)
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

    private fun downloadApk(packageName: String) {
        packageInfoViewModel.downloadAppPackage(packageName)
            .observeUntil(
                this@HomeActivity,
                { appPackageDownloadStatus ->
                    appPackageDownloadStatus?.state in arrayListOf(
                        WorkInfo.State.SUCCEEDED,
                        WorkInfo.State.FAILED,
                        WorkInfo.State.CANCELLED
                    )
                }
            ) {
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

        private const val REQUEST_STORAGE_PERMISSIONS = 0
        private const val REQUEST_CODE_SYNC = 0
    }
}
