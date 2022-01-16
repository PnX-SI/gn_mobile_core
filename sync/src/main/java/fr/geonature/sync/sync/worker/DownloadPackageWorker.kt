package fr.geonature.sync.sync.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.geonature.datasync.api.IGeoNatureAPIClient
import fr.geonature.sync.sync.IPackageInfoManager
import fr.geonature.sync.sync.PackageInfo
import okhttp3.ResponseBody
import okhttp3.internal.closeQuietly
import okio.Buffer
import okio.buffer
import okio.sink
import retrofit2.awaitResponse
import java.io.File

/**
 * Download given application package.
 *
 * @author S. Grimault
 */
@HiltWorker
class DownloadPackageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val geoNatureAPIClient: IGeoNatureAPIClient,
    private val packageInfoManager: IPackageInfoManager
) : CoroutineWorker(
    appContext,
    workerParams
) {
    override suspend fun doWork(): Result {
        val packageName = inputData.getString(KEY_PACKAGE_NAME)

        if (packageName.isNullOrBlank()) {
            return Result.failure()
        }

        val packageInfoToUpdate = packageInfoManager.getPackageInfo(packageName)
            ?: return Result.failure()
        val apkUrl = packageInfoToUpdate.apkUrl
            ?: return Result.failure()

        Log.i(
            TAG,
            "updating '$packageName'..."
        )

        setProgress(workData(packageInfoToUpdate.packageName))

        return try {
            val response = geoNatureAPIClient
                .downloadPackage(apkUrl)
                .awaitResponse()

            if (response.isSuccessful) {
                return Result.failure(
                    workData(
                        packageInfoToUpdate.packageName,
                        100
                    )
                )
            }

            val responseBody = response.body()
                ?: return Result.failure(
                    workData(
                        packageInfoToUpdate.packageName,
                        100
                    )
                )

            downloadAsFile(
                responseBody,
                packageInfoToUpdate
            )
        } catch (e: Exception) {
            Log.w(
                TAG,
                e
            )

            Result.failure(
                workData(
                    packageInfoToUpdate.packageName,
                    100
                )
            )
        }
    }

    private fun downloadAsFile(
        responseBody: ResponseBody,
        packageInfo: PackageInfo
    ): Result {
        val source = responseBody.source()
        val contentLength = responseBody.contentLength()

        val apkFilePath =
            "${applicationContext.getExternalFilesDir(null)?.absolutePath}/${packageInfo.packageName}_${packageInfo.versionCode}.apk"

        val buffer = Buffer()
        val bufferedSink = File(apkFilePath)
            .sink()
            .buffer()
        var total = 0L

        while (true) {
            val read: Long = source
                .read(
                    buffer,
                    4096
                )
                .takeUnless { it == -1L }
                ?: break

            bufferedSink.write(
                buffer,
                read
            )
            val previousProgress = (100 * total / contentLength.toFloat()).toInt()
            total += read
            val currentProgress = (100 * total / contentLength.toFloat()).toInt()

            if (currentProgress > 0 && currentProgress > previousProgress) {
                setProgressAsync(
                    workData(
                        packageInfo.packageName,
                        currentProgress
                    )
                )
            }
        }

        source.closeQuietly()
        bufferedSink.closeQuietly()
        responseBody.closeQuietly()

        setProgressAsync(
            workData(
                packageInfo.packageName,
                100
            )
        )

        return Result.success(
            workData(
                packageInfo.packageName,
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
