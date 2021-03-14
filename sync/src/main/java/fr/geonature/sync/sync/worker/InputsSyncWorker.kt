package fr.geonature.sync.sync.worker

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import fr.geonature.sync.api.GeoNatureAPIClient
import fr.geonature.sync.sync.PackageInfo
import fr.geonature.sync.sync.PackageInfoManager
import fr.geonature.sync.sync.SyncInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import java.io.File

/**
 * Inputs synchronization worker from given [PackageInfo].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {
    private val packageInfoManager =
        PackageInfoManager.getInstance(applicationContext)

    override suspend fun doWork(): Result {
        val packageName = inputData.getString(KEY_PACKAGE_NAME)

        if (packageName.isNullOrBlank()) {
            return Result.failure()
        }

        val packageInfo: PackageInfo =
            packageInfoManager.getPackageInfo(packageName) ?: return Result.failure()

        val geoNatureAPIClient = GeoNatureAPIClient.instance(applicationContext)
            ?: return Result.failure()

        Log.i(
            TAG,
            "starting inputs synchronization for '$packageName'..."
        )

        NotificationManagerCompat.from(applicationContext)
            .cancel(CheckInputsToSynchronizeWorker.SYNC_NOTIFICATION_ID)

        setProgress(
            workData(
                packageInfo.packageName,
                WorkInfo.State.RUNNING
            )
        )

        val inputsToSynchronize = packageInfoManager.getInputsToSynchronize(packageInfo)
        val inputsSynchronized = mutableListOf<SyncInput>()

        if (inputsToSynchronize.isEmpty()) {
            setProgress(
                workData(
                    packageInfo.packageName,
                    WorkInfo.State.CANCELLED
                )
            )

            Log.i(
                TAG,
                "No inputs to synchronize for '$packageName'"
            )

            return Result.success()
        }

        setProgress(
            workData(
                packageInfo.packageName,
                WorkInfo.State.RUNNING,
                inputsToSynchronize.size
            )
        )

        inputsToSynchronize.forEach { syncInput ->
            try {
                val response = geoNatureAPIClient.sendInput(
                    syncInput.module,
                    syncInput.payload
                )
                    .awaitResponse()

                if (!response.isSuccessful) {
                    setProgress(
                        workData(
                            packageInfo.packageName,
                            WorkInfo.State.FAILED,
                            inputsToSynchronize.filter { filtered -> !inputsSynchronized.contains(filtered) }.size
                        )
                    )
                    delay(1000)

                    return@forEach
                }

                deleteSynchronizedInput(syncInput).takeIf { deleted -> deleted }
                    ?.also {
                        inputsSynchronized.add(syncInput)
                        setProgress(
                            workData(
                                packageInfo.packageName,
                                WorkInfo.State.RUNNING,
                                inputsToSynchronize.filter { filtered -> !inputsSynchronized.contains(filtered) }.size
                            )
                        )
                    }
            } catch (e: Exception) {
                Log.w(
                    TAG,
                    e
                )

                setProgress(
                    workData(
                        packageInfo.packageName,
                        WorkInfo.State.FAILED,
                        inputsToSynchronize.filter { filtered -> !inputsSynchronized.contains(filtered) }.size
                    )
                )
                delay(1000)
            }
        }

        Log.i(
            TAG,
            "inputs synchronization ${if (inputsSynchronized.size == inputsToSynchronize.size) "successfully finished" else "finished with errors"} for '$packageName'"
        )

        return if (inputsSynchronized.size == inputsToSynchronize.size) {
            Result.success(
                workData(
                    packageInfo.packageName,
                    WorkInfo.State.SUCCEEDED,
                    inputsToSynchronize.filter { filtered -> !inputsSynchronized.contains(filtered) }.size
                )
            )
        } else {
            Result.failure(
                workData(
                    packageInfo.packageName,
                    WorkInfo.State.FAILED,
                    inputsToSynchronize.filter { filtered -> !inputsSynchronized.contains(filtered) }.size
                )
            )
        }
    }

    private suspend fun deleteSynchronizedInput(syncInput: SyncInput): Boolean {
        val deleted = withContext(Dispatchers.IO) {
            File(syncInput.filePath).takeIf { it.exists() && it.isFile && it.parentFile?.canWrite() ?: false }
                ?.delete() ?: false
        }

        if (deleted) {
            packageInfoManager.getInputsToSynchronize(syncInput.packageInfo)
        }

        return deleted
    }

    private fun workData(packageName: String, state: WorkInfo.State, inputs: Int = 0): Data {
        return workDataOf(
            KEY_PACKAGE_NAME to packageName,
            KEY_PACKAGE_STATUS to state.ordinal,
            KEY_PACKAGE_INPUTS to inputs
        )
    }

    companion object {
        private val TAG = InputsSyncWorker::class.java.name

        const val KEY_PACKAGE_NAME = "KEY_PACKAGE_NAME"
        const val KEY_PACKAGE_STATUS = "KEY_PACKAGE_STATUS"
        const val KEY_PACKAGE_INPUTS = "KEY_PACKAGE_INPUTS"
        const val INPUT_SYNC_WORKER_TAG = "inputs_sync_worker_tag"

        val workName: (packageName: String) -> String = { "inputs_sync_worker:$it" }
    }
}
