package fr.geonature.sync.sync.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import fr.geonature.sync.api.GeoNatureAPIClient
import fr.geonature.sync.api.model.AppPackage
import fr.geonature.sync.sync.PackageInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okhttp3.internal.Util
import okio.Buffer
import okio.Okio
import retrofit2.awaitResponse
import java.io.File

/**
 * Download given application package.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class DownloadPackageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {
    private val packageInfoManager =
        PackageInfoManager.getInstance(applicationContext)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val packageName = inputData.getString(KEY_PACKAGE_NAME)

        if (packageName.isNullOrBlank()) {
            return@withContext Result.failure()
        }

        val appPackageToUpdate =
            packageInfoManager.getAppPackageToUpdate(packageName)
                ?: return@withContext Result.failure()

        val geoNatureAPIClient = GeoNatureAPIClient.instance(applicationContext)
            ?: return@withContext Result.failure()

        Log.i(
            TAG,
            "updating '$packageName'..."
        )

        setProgress(workData(appPackageToUpdate.packageName))

        try {
            // update app settings as JSON file
            packageInfoManager.updateAppSettings(appPackageToUpdate)

            val response = geoNatureAPIClient.downloadPackage(appPackageToUpdate.apk)
                .awaitResponse()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    workData(
                        appPackageToUpdate.packageName,
                        100
                    )
                )
            }

            val responseBody = response.body() ?: return@withContext Result.failure(
                workData(
                    appPackageToUpdate.packageName,
                    100
                )
            )

            downloadAsFile(
                responseBody,
                appPackageToUpdate
            )
        } catch (e: Exception) {
            Log.w(
                TAG,
                e
            )

            Result.failure(
                workData(
                    appPackageToUpdate.packageName,
                    100
                )
            )
        }
    }

    private fun downloadAsFile(
        responseBody: ResponseBody,
        appPackage: AppPackage
    ): Result {
        val source = responseBody.source()
        val contentLength = responseBody.contentLength()

        val apkFilePath =
            "${applicationContext.getExternalFilesDir(null)?.absolutePath}/${appPackage.packageName}_${appPackage.versionCode}.apk"

        val buffer = Buffer()
        val bufferedSink = Okio.buffer(Okio.sink(File(apkFilePath)))
        var total = 0L

        while (true) {
            val read: Long = source.read(
                buffer,
                4096
            )
                .takeUnless { it == -1L } ?: break

            bufferedSink.write(
                buffer,
                read
            )
            val previousProgress = (100 * total / contentLength.toFloat()).toInt()
            total += read
            val currentProgress = (100 * total / contentLength.toFloat()).toInt()

            if (currentProgress > 0 && currentProgress > previousProgress) {
                Log.d(
                    TAG,
                    "downloading '${appPackage.packageName}': $currentProgress"
                )

                setProgressAsync(
                    workData(
                        appPackage.packageName,
                        currentProgress
                    )
                )
            }
        }

        Util.closeQuietly(source)
        Util.closeQuietly(bufferedSink)
        Util.closeQuietly(responseBody)

        return Result.success(
            workData(
                appPackage.packageName,
                100,
                apkFilePath
            )
        )
    }

    private fun workData(
        packageName: String,
        progress: Int = -1,
        apkFilePath: String? = null
    ): Data {
        return workDataOf(
            KEY_PACKAGE_NAME to packageName,
            KEY_PROGRESS to progress,
            KEY_APK_FILE_PATH to apkFilePath
        )
    }

    companion object {
        private val TAG = DownloadPackageWorker::class.java.name

        const val DOWNLOAD_PACKAGE_WORKER_TAG = "download_package_worker_tag"
        const val KEY_PACKAGE_NAME = "KEY_PACKAGE_NAME"
        const val KEY_PROGRESS = "KEY_PROGRESS"
        const val KEY_APK_FILE_PATH = "KEY_APK_FILE_PATH"

        val workName: (packageName: String) -> String = { "download_package_worker:$it" }
    }
}