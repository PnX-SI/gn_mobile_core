package fr.geonature.sync.worker

/**
 * Defines a list of constants used for [androidx.work.Worker] names, inputs & outputs.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object Constants {

    // The name of the synchronisation work
    const val SYNC_WORK_NAME = "sync_work_name"

    const val TAG_SYNC_OUTPUT = "tag_sync_output"
}