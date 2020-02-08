package fr.geonature.commons.input

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
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
            .values.mapNotNull { if (it is String && !it.isBlank()) inputJsonReader.read(it) else null }
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
    suspend fun saveInput(input: I): Boolean = withContext(IO) {
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

        preferenceManager.contains(buildInputPreferenceKey(input.id))
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
    suspend fun deleteInput(id: Long): Boolean = withContext(IO) {
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

        !preferenceManager.contains(buildInputPreferenceKey(id)).also {
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
    suspend fun exportInput(id: Long): Boolean = withContext(IO) {
        val inputToExport = readInput(id) ?: return@withContext false

        val inputExportFile = getInputExportFile(inputToExport)
        inputJsonWriter.write(
            FileWriter(inputExportFile),
            inputToExport
        )

        return@withContext if (inputExportFile.exists() && inputExportFile.length() > 0) {
            deleteInput(id)
        } else {
            false
        }
    }

    /**
     * Exports [AbstractInput] as `JSON` file.
     *
     * @param input the [AbstractInput] to save
     *
     * @return `true` if the given [AbstractInput] has been successfully exported, `false` otherwise
     */
    suspend fun exportInput(input: I): Boolean = withContext(IO) {
        val inputExportFile = getInputExportFile(input)
        inputJsonWriter.write(
            FileWriter(inputExportFile),
            input
        )

        return@withContext if (inputExportFile.exists() && inputExportFile.length() > 0) {
            deleteInput(input.id)
        } else {
            false
        }
    }

    private fun buildInputPreferenceKey(id: Long): String {
        return "${KEY_PREFERENCE_INPUT}_$id"
    }

    @Throws(IOException::class)
    private fun getInputExportFile(input: AbstractInput): File {
        val inputDir = FileUtils.getInputsFolder(application)
        inputDir.mkdirs()

        return File(
            inputDir,
            "input_${input.module}_${input.id}.json"
        )
    }

    companion object {
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
