package fr.geonature.datasync.packageinfo

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

/**
 * Gets installed [PackageInfo].
 */
class InstalledPackageInfoDataSourceImpl(private val applicationContext: Context) :
    IPackageInfoDataSource {

    private val pm = applicationContext.packageManager

    @SuppressLint("QueryPermissionsNeeded")
    override suspend fun getAll(): List<PackageInfo> {
        return pm
            .getInstalledApplications(PackageManager.GET_META_DATA)
            .asFlow()
            .filter { it.packageName.startsWith(applicationContext.packageName.substringBeforeLast(".")) }
            .map {
                val packageInfoFromPackageManager = pm.getPackageInfo(it.packageName,
                    PackageManager.GET_META_DATA)

                @Suppress("DEPRECATION") PackageInfo(it.packageName,
                    pm
                        .getApplicationLabel(it)
                        .toString(),
                    0,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfoFromPackageManager.longVersionCode
                    else packageInfoFromPackageManager.versionCode.toLong(),
                    packageInfoFromPackageManager.versionName,
                    null,
                    pm.getApplicationIcon(it.packageName),
                    pm.getLaunchIntentForPackage(it.packageName)).apply {
                    inputsStatus = AppPackageInputsStatus(it.packageName,
                        WorkInfo.State.ENQUEUED,
                        getInputsToSynchronize(applicationContext).size)
                }
            }
            .toList()
    }
}