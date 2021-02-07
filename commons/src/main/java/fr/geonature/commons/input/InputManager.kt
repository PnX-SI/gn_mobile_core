package fr.geonature.commons.input

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.input.io.InputJsonWriter
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Manage [AbstractInput]:
 * - Create a new [AbstractInput]
 * - Read the current [AbstractInput]
 * - Save the current [AbstractInput]
 * - Export the current [AbstractInput] as `JSON` file
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputManager<I : AbstractInput> private constructor(
    internal val application: Application,
    inputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<I>,
    inputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<I>
) {

    internal val preferenceManager: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(application)
    private val inputJsonReader: InputJsonReader<I> = InputJsonReader(inputJsonReaderListener)
    private val inputJsonWriter: InputJsonWriter<I> = InputJsonWriter(inputJsonWriterListener)

    val inputs: MutableLiveData<List<I>> = MutableLiveData()
    val input: MutableLiveData<I> = MutableLiveData()

    /**
     * Reads all [AbstractInput]s.
     *
     * @return A list of [AbstractInput]s
     */
    suspend fun readInputs(): List<I> = withContext(IO) {
        preferenceManager.all.filterKeys { it.startsWith("${KEY_PREFERENCE_INPUT}_") }
            .values.mapNotNull { if (it is String && it.isNotBlank()) inputJsonReader.read(it) else null }
            .sortedBy { it.id }
            .also { inputs.postValue(it) }
    }

    /**
     * Reads [AbstractInput] from given ID.
     *
     * @param id The [AbstractInput] ID to read. If omitted, read the current saved [AbstractInput].
     *
     * @return [AbstractInput] or `null` if not found
     */
    suspend fun readInput(id: Long? = null): I? = withContext(IO) {
        val inputPreferenceKey = buildInputPreferenceKey(
            id
                ?: preferenceManager.getLong(
                    KEY_PREFERENCE_CURRENT_INPUT,
                    0
                )
        )
        val inputAsJson = preferenceManager.getString(
            inputPreferenceKey,
            null
        )

        if (inputAsJson.isNullOrBlank()) {
            return@withContext null
        }

        inputJsonReader.read(inputAsJson)
            .also { input.postValue(it) }
    }

    /**
     * Reads the current [AbstractInput].
     *
     * @return [AbstractInput] or `null` if not found
     */
    suspend fun readCurrentInput(): I? {
        return readInput()
    }

    /**
     * Saves the given [AbstractInput] and sets it as default current [AbstractInput].
     *
     * @param input the [AbstractInput] to save
     *
     * @return `true` if the given [AbstractInput] has been successfully saved, `false` otherwise
     */
    @SuppressLint("ApplySharedPref")
    suspend fun saveInput(input: I): Boolean {
        val saved = withContext(IO) {
            val inputAsJson = inputJsonWriter.write(input)
            if (inputAsJson.isNullOrBlank()) return@withContext false

            preferenceManager.edit()
                .putString(
                    buildInputPreferenceKey(input.id),
                    inputAsJson
                )
                .putLong(
                    KEY_PREFERENCE_CURRENT_INPUT,
                    input.id
                )
                .commit()
        }

        return saved && preferenceManager.contains(buildInputPreferenceKey(input.id))
            .also { readInputs() }
    }

    /**
     * Deletes [AbstractInput] from given ID.
     *
     * @param id the [AbstractInput] ID to delete
     *
     * @return `true` if the given [AbstractInput] has been successfully deleted, `false` otherwise
     */
    @SuppressLint("ApplySharedPref")
    suspend fun deleteInput(id: Long): Boolean {
        val deleted = withContext(IO) {
            preferenceManager.edit()
                .remove(buildInputPreferenceKey(id))
                .also {
                    if (preferenceManager.getLong(
                            KEY_PREFERENCE_CURRENT_INPUT,
                            0
                        ) == id
                    ) {
                        it.remove(KEY_PREFERENCE_CURRENT_INPUT)
                    }
                }
                .commit()
        }

        Log.i(
            TAG,
            "input '$id' deleted: $deleted"
        )

        return deleted && !preferenceManager.contains(buildInputPreferenceKey(id))
            .also {
                readInputs()
                input.postValue(null)
            }
    }

    /**
     * Exports [AbstractInput] from given ID as `JSON` file.
     *
     * @param id the [AbstractInput] ID to export
     *
     * @return `true` if the given [AbstractInput] has been successfully exported, `false` otherwise
     */
    suspend fun exportInput(id: Long): Boolean {
        val inputToExport = readInput(id) ?: return false

        return exportInput(inputToExport)
    }

    /**
     * Exports [AbstractInput] as `JSON` file.
     *
     * @param input the [AbstractInput] to save
     *
     * @return `true` if the given [AbstractInput] has been successfully exported, `false` otherwise
     */
    suspend fun exportInput(input: I): Boolean {
        val inputExportFile = getInputExportFile(input)

        @Suppress("BlockingMethodInNonBlockingContext")
        withContext(IO) {
            inputJsonWriter.write(
                FileWriter(inputExportFile),
                input
            )
        }

        Log.i(
            TAG,
            "export input '${input.id}' to JSON file '${inputExportFile.absolutePath}'"
        )
        Log.d(
            TAG,
            "'${inputExportFile.absolutePath}' exists? ${inputExportFile.exists()}"
        )

        return if (inputExportFile.exists()) {
            deleteInput(input.id)
        } else {
            false
        }
    }

    private fun buildInputPreferenceKey(id: Long): String {
        return "${KEY_PREFERENCE_INPUT}_$id"
    }

    @Throws(IOException::class)
    private suspend fun getInputExportFile(input: AbstractInput): File = withContext(IO) {
        val inputDir = FileUtils.getInputsFolder(application)
        inputDir.mkdirs()

        return@withContext File(
            inputDir,
            "input_${input.module}_${input.id}.json"
        )
    }

    companion object {
        private val TAG = InputManager::class.java.name

        private const val KEY_PREFERENCE_INPUT = "key_preference_input"
        private const val KEY_PREFERENCE_CURRENT_INPUT = "key_preference_current_input"

        @Volatile
        private var INSTANCE: InputManager<*>? = null

        /**
         * Gets the singleton instance of [InputManager].
         *
         * @param application The main application context.
         *
         * @return The singleton instance of [InputManager].
         */
        @Suppress("UNCHECKED_CAST")
        fun <I : AbstractInput> getInstance(
            application: Application,
            inputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<I>,
            inputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<I>
        ): InputManager<I> = INSTANCE as InputManager<I>?
            ?: synchronized(this) {
                INSTANCE as InputManager<I>? ?: InputManager(
                    application,
                    inputJsonReaderListener,
                    inputJsonWriterListener
                ).also { INSTANCE = it }
            }
    }
}
