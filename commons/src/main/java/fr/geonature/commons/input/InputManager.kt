package fr.geonature.commons.input

import android.app.Application
import android.preference.PreferenceManager
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.input.io.InputJsonWriter
import fr.geonature.commons.util.FileUtils
import fr.geonature.commons.util.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.Writer

/**
 * Manage [AbstractInput]:
 * - Create a new [AbstractInput]
 * - Read the current [AbstractInput]
 * - Save the current [AbstractInput]
 * - Export the current [AbstractInput] as `JSON` file
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputManager(private val application: Application,
                   inputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener,
                   inputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener) {

    private val preferenceManager = PreferenceManager.getDefaultSharedPreferences(application)
    private val inputJsonReader: InputJsonReader = InputJsonReader(inputJsonReaderListener)
    private val inputJsonWriter: InputJsonWriter = InputJsonWriter(inputJsonWriterListener)

    suspend fun readInput(id: Long? = null): AbstractInput? = withContext(IO) {
        val inputPreferenceKey =
            buildInputPreferenceKey(id ?: preferenceManager.getLong(KEY_PREFERENCE_CURRENT_INPUT,
                                                                    0))
        val inputAsJson = preferenceManager.getString(inputPreferenceKey,
                                                      null)

        if (StringUtils.isEmpty(inputAsJson)) {
            return@withContext null
        }

        inputJsonReader.read(inputAsJson)
    }

    suspend fun readCurrentInput(): AbstractInput? {
        return readInput()
    }

    suspend fun saveInput(input: AbstractInput): Boolean = withContext(IO) {
        val inputAsJson = inputJsonWriter.write(input)

        if (StringUtils.isEmpty(inputAsJson)) return@withContext false

        preferenceManager.edit()
            .putString(buildInputPreferenceKey(input.id),
                       inputAsJson)
            .putLong(KEY_PREFERENCE_CURRENT_INPUT,
                     input.id)
            .apply()

        preferenceManager.contains(buildInputPreferenceKey(input.id))
    }

    suspend fun deleteInput(id: Long): Boolean = withContext(IO) {
        preferenceManager.edit()
            .remove(buildInputPreferenceKey(id))
            .also {
                if (preferenceManager.getLong(KEY_PREFERENCE_CURRENT_INPUT,
                                              0) == id) {
                    it.remove(KEY_PREFERENCE_CURRENT_INPUT)
                }
            }
            .apply()

        !preferenceManager.contains(buildInputPreferenceKey(id))
    }

    suspend fun exportInput(id: Long): Boolean = coroutineScope {
        val inputToExport = withContext(Dispatchers.Default) { readInput(id) } ?: return@coroutineScope false

        val exported = withContext(IO) {
            inputJsonWriter.write(getInputExportWriter(inputToExport),
                                  inputToExport)

            true
        }
        val deleted = deleteInput(id)

        return@coroutineScope deleted && exported
    }

    private fun buildInputPreferenceKey(id: Long): String {
        return "${KEY_PREFERENCE_INPUT}_$id"
    }

    @Throws(IOException::class)
    private fun getInputExportWriter(input: AbstractInput): Writer {
        val inputDir = FileUtils.getInputsFolder(application)

        inputDir.mkdirs()

        val inputFile = File(inputDir,
                             "input_${input.module}_${input.id}.json")

        return FileWriter(inputFile)
    }

    companion object {
        private const val KEY_PREFERENCE_INPUT = "key_preference_input"
        private const val KEY_PREFERENCE_CURRENT_INPUT = "key_preference_current_input"
    }
}
