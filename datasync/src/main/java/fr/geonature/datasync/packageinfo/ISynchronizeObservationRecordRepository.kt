package fr.geonature.datasync.packageinfo

/**
 * Synchronize observation record.
 *
 * @author S. Grimault
 */
interface ISynchronizeObservationRecordRepository {

    suspend operator fun invoke(recordId: Long): Result<Unit>
}