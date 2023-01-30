package fr.geonature.datasync.packageinfo.worker

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import fr.geonature.datasync.packageinfo.IPackageInfoRepository
import fr.geonature.datasync.packageinfo.ISynchronizeObservationRecordRepository
import fr.geonature.datasync.packageinfo.PackageInfo
import fr.geonature.datasync.packageinfo.SyncInput
import kotlinx.coroutines.delay
import org.tinylog.Logger

/**
 * Inputs synchronization worker from given [PackageInfo].
 *
 * @author S. Grimault
 */
@HiltWorker
class InputsSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val packageInfoRepository: IPackageInfoRepository,
    private val synchronizeRecordRepository: ISynchronizeObservationRecordRepository
) : CoroutineWorker(
    appContext,
    workerParams
) {
    override suspend fun doWork(): Result {
        val packageName = inputData.getString(KEY_PACKAGE_NAME)

        if (packageName.isNullOrBlank()) {
            return Result.failure()
        }

        val packageInfo: PackageInfo = packageInfoRepository.getPackageInfo(packageName)
            ?: return Result.failure()

        NotificationManagerCompat
            .from(applicationContext)
            .cancel(CheckInputsToSynchronizeWorker.SYNC_NOTIFICATION_ID)

        setProgress(
            workData(
                packageInfo.packageName,
                WorkInfo.State.RUNNING
            )
        )

        val inputsToSynchronize = packageInfo.getInputsToSynchronize(applicationContext)
        val inputsSynchronized = mutableListOf<SyncInput>()

        if (inputsToSynchronize.isEmpty()) {
            setProgress(
                workData(
                    packageInfo.packageName,
                    WorkInfo.State.CANCELLED
                )
            )

            Logger.info { "no observation records to synchronize for '$packageName'" }

            return Result.success()
        }

        Logger.info { "${inputsToSynchronize.size} observation record(s) to synchronize for '$packageName'..." }

        setProgress(
            workData(
                packageInfo.packageName,
                WorkInfo.State.RUNNING,
                inputsToSynchronize.size
            )
        )

        inputsToSynchronize.forEach { syncInput ->
            val synchronizeInputResult = synchronizeRecordRepository(syncInput.id)

            if (synchronizeInputResult.isFailure) {
                (synchronizeInputResult.exceptionOrNull()?.message
                    ?: "failed to synchronize observation record '${syncInput.id}'").also {
                    Logger.warn { it }
                }

                setProgress(
                    workData(
                        packageInfo.packageName,
                        WorkInfo.State.FAILED,
                        inputsToSynchronize.size - inputsSynchronized.size
                    )
                )

                delay(1000)

                return@forEach
            }

            inputsSynchronized.add(syncInput)

            setProgress(
                workData(
                    packageInfo.packageName,
                    WorkInfo.State.RUNNING,
                    inputsToSynchronize.size - inputsSynchronized.size
                )
            )
        }

        Logger.info {
            "observation records synchronization ${if (inputsSynchronized.size == inputsToSynchronize.size) "successfully finished" else "finished with errors"} for '$packageName'"
        }

        return if (inputsSynchronized.size == inputsToSynchronize.size) {
            Result.success(
                workData(
                    packageInfo.packageName,
                    WorkInfo.State.SUCCEEDED,
                    0
                )
            )
        } else {
            Result.failure(
                workData(
                    packageInfo.packageName,
                    WorkInfo.State.FAILED,
                    inputsToSynchronize.size - inputsSynchronized.size
                )
            )
        }
    }

    private fun workData(
        packageName: String,
        state: WorkInfo.State,
        inputs: Int = 0
    ): Data {
        return workDataOf(
            KEY_PACKAGE_NAME to packageName,
            KEY_PACKAGE_STATUS to state.ordinal,
            KEY_PACKAGE_INPUTS to inputs
        )
    }

    companion object {

        const val KEY_PACKAGE_NAME = "KEY_PACKAGE_NAME"
        const val KEY_PACKAGE_STATUS = "KEY_PACKAGE_STATUS"
        const val KEY_PACKAGE_INPUTS = "KEY_PACKAGE_INPUTS"
        const val INPUT_SYNC_WORKER_TAG = "inputs_sync_worker_tag"

        val workName: (packageName: String) -> String = { "inputs_sync_worker:$it" }
    }
}
