package fr.geonature.sync.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import fr.geonature.commons.util.FileUtils.getInputsFolder
import fr.geonature.sync.api.GeoNatureAPIClient
import fr.geonature.sync.util.SettingsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Input synchronization worker.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputsSyncWorker(appContext: Context,
                       workerParams: WorkerParameters) : CoroutineWorker(appContext,
                                                                         workerParams) {

    private val packageInfoManager = PackageInfoManager.getInstance(applicationContext)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val packageName = inputData.getString(KEY_PACKAGE_NAME)

        if (packageName.isNullOrBlank()) {
            return@withContext Result.failure()
        }

        val packageInfo = packageInfoManager.packageInfos.value?.firstOrNull { it.packageName == packageName }
            ?: return@withContext Result.failure()

        val geoNatureServerUrl = SettingsUtils.getGeoNatureServerUrl(applicationContext)

        if (geoNatureServerUrl.isNullOrBlank()) {
            Log.w(TAG,
                  "No GeoNature server configured")

            return@withContext Result.failure()
        }

        Log.i(TAG,
              "starting inputs synchronization for '$packageName'...")

        packageInfoManager.updatePackageInfo(packageInfo.packageName,
                                             WorkInfo.State.RUNNING,
                                             emptyList())

        val inputsToSynchronize = getInputsToSynchronize(packageInfo)
        val inputsSynchronized = mutableListOf<SyncInput>()

        if (inputsToSynchronize.isEmpty()) {
            packageInfoManager.updatePackageInfo(packageInfo.packageName,
                                                 WorkInfo.State.CANCELLED,
                                                 emptyList())

            Log.i(TAG,
                  "No inputs to synchronize for '$packageName'")

            return@withContext Result.success()
        }

        packageInfoManager.updatePackageInfo(packageInfo.packageName,
                                             WorkInfo.State.RUNNING,
                                             inputsToSynchronize)

        val geoNatureServiceClient = GeoNatureAPIClient.instance(applicationContext,
                                                                 geoNatureServerUrl)
                .value
        inputsToSynchronize.forEach { syncInput ->
            try {
                val response = geoNatureServiceClient.sendInput(syncInput.module,
                                                                syncInput.payload)
                        .execute()

                if (!response.isSuccessful) {
                    packageInfoManager.updatePackageInfo(packageInfo.packageName,
                                                         WorkInfo.State.FAILED,
                                                         inputsToSynchronize)
                    delay(1000)

                    return@forEach
                }

                deleteSynchronizedInput(syncInput).takeIf { deleted -> deleted }
                        ?.also {
                            inputsSynchronized.add(syncInput)
                            packageInfoManager.updatePackageInfo(packageInfo.packageName,
                                                                 WorkInfo.State.RUNNING,
                                                                 inputsToSynchronize.filter { filtered -> !inputsSynchronized.contains(filtered) })
                        }
            }
            catch (e: Exception) {
                packageInfoManager.updatePackageInfo(packageInfo.packageName,
                                                     WorkInfo.State.FAILED,
                                                     inputsToSynchronize)
                delay(1000)
            }
        }

        packageInfoManager.updatePackageInfo(packageInfo.packageName,
                                             if (inputsSynchronized.size == inputsToSynchronize.size) WorkInfo.State.SUCCEEDED else WorkInfo.State.FAILED,
                                             inputsToSynchronize.filter { filtered -> !inputsSynchronized.contains(filtered) })

        Log.i(TAG,
              "inputs synchronization ${if (inputsSynchronized.size == inputsToSynchronize.size) "successfully finished" else "finished with errors"} for '$packageName'")

        Result.success()
    }

    private suspend fun getInputsToSynchronize(packageInfo: PackageInfo): List<SyncInput> = withContext(Dispatchers.IO) {
        getInputsFolder(applicationContext,
                        packageInfo.packageName).walkTopDown()
                .filter { f -> f.isFile && f.extension == "json" && f.canRead() }
                .map {
                    val rawString = it.readText()
                    val toJson = JSONObject(rawString)
                    SyncInput(packageInfo,
                              it.absolutePath,
                              toJson.getString("module"),
                              toJson)
                }
                .toList()
    }

    private suspend fun deleteSynchronizedInput(syncInput: SyncInput): Boolean = withContext(Dispatchers.IO) {
        true
        // TODO: delete input if successfully synchronized
        /*
        File(syncInput.filePath).takeIf { it.exists() && it.isFile && it.parentFile.canWrite() }?.delete()?.also {
            getInputsToSynchronize(syncInput.packageInfo)
        } ?: false
         */
    }

    companion object {
        private val TAG = InputsSyncWorker::class.java.name

        const val KEY_PACKAGE_NAME = "KEY_PACKAGE_NAME"

        val workName: (packageName: String) -> String = { "inputs_sync_worker:$it" }
        val tagName: (packageName: String) -> String = { "inputs_sync_worker_tag:$it" }
    }
}