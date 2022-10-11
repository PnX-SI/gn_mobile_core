package fr.geonature.commons.features.input.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.error.InputException
import fr.geonature.commons.features.input.io.InputJsonReader
import fr.geonature.commons.features.input.io.InputJsonWriter
import fr.geonature.commons.settings.IAppSettings
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import java.io.File

/**
 * Default implementation of [IInputLocalDataSource] using [SharedPreferences].
 *
 * @author S. Grimault
 */
class InputLocalDataSourceImpl<I : AbstractInput, S : IAppSettings>(
    private val context: Context,
    inputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<I>,
    inputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<I, S>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IInputLocalDataSource<I, S> {

    private val preferenceManager: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    private val inputJsonReader: InputJsonReader<I> = InputJsonReader(inputJsonReaderListener)
    private val inputJsonWriter: InputJsonWriter<I, S> = InputJsonWriter(inputJsonWriterListener)

    override suspend fun readInputs(): List<I> {
        val exportedInput = FileUtils
            .getInputsFolder(context)
            .walkTopDown()
            .asFlow()
            .filter { it.isFile && it.extension == "json" }
            .filter { it.nameWithoutExtension.startsWith("input") }
            .filter { it.canRead() }
            .map {
                val input = inputJsonReader.read(it.readText())

                if (input == null) {
                    Logger.warn { "invalid input file found '${it.name}'" }

                    it.delete()

                    return@map null
                }

                input
            }
            .filterNotNull()
            .toList()

        return withContext(dispatcher) {
            (exportedInput + preferenceManager.all.filterKeys { it.startsWith("${KEY_PREFERENCE_INPUT}_") }.values.mapNotNull { if (it is String && it.isNotBlank()) inputJsonReader.read(it) else null }).sortedBy { it.id }
        }
    }

    override suspend fun readInput(id: Long): I =
        withContext(dispatcher) {
            val inputAsJson = preferenceManager.getString(
                buildInputPreferenceKey(id),
                null
            )
                ?: File(
                    FileUtils
                        .getInputsFolder(context)
                        .also { it.mkdirs() },
                    "input_${id}.json"
                )
                    .takeIf { it.exists() }
                    ?.readText()

            if (inputAsJson.isNullOrBlank()) {
                throw InputException.NotFoundException(id)
            }

            inputJsonReader.read(inputAsJson)
                ?: throw InputException.ReadException(id)
        }

    override suspend fun saveInput(
        input: I,
        status: AbstractInput.Status
    ) =
        withContext(dispatcher) {
            val savedInput = input.apply { this.status = status }
            val inputAsJson = inputJsonWriter.write(savedInput)
            if (inputAsJson.isNullOrBlank()) throw InputException.WriteException(savedInput.id)

            val saved = preferenceManager
                .edit()
                .putString(
                    buildInputPreferenceKey(savedInput.id),
                    inputAsJson
                )
                .commit()

            if (!saved) throw InputException.WriteException(savedInput.id)

            File(
                FileUtils
                    .getInputsFolder(context)
                    .also { it.mkdirs() },
                "input_${savedInput.id}.json"
            )
                .takeIf { it.exists() }
                ?.delete()

            savedInput
        }

    override suspend fun deleteInput(id: Long) =
        withContext(dispatcher) {
            File(
                FileUtils
                    .getInputsFolder(context)
                    .also { it.mkdirs() },
                "input_${id}.json"
            )
                .takeIf { it.exists() }
                ?.delete()

            if (preferenceManager.contains(buildInputPreferenceKey(id))) {
                val deleted = preferenceManager
                    .edit()
                    .remove(buildInputPreferenceKey(id))
                    .commit()

                if (!deleted) throw InputException.WriteException(id)
            }
        }

    override suspend fun exportInput(
        id: Long,
        settings: S?
    ): I {
        val inputToExport = readInput(id)

        return exportInput(
            inputToExport,
            settings
        )
    }

    override suspend fun exportInput(
        input: I,
        settings: S?
    ): I {
        val inputToSync = input.apply { this.status = AbstractInput.Status.TO_SYNC }
        deleteInput(input.id)

        return withContext(dispatcher) {
            val inputAsJson = inputJsonWriter.write(inputToSync)
            if (inputAsJson.isNullOrBlank()) throw InputException.WriteException(inputToSync.id)

            File(
                FileUtils
                    .getInputsFolder(context)
                    .also { it.mkdirs() },
                "input_${inputToSync.id}.json"
            )
                .bufferedWriter()
                .use { out ->
                    out.write(inputAsJson)
                    out.flush()
                    out.close()
                }

            inputToSync
        }
    }

    private fun buildInputPreferenceKey(id: Long): String {
        return "${KEY_PREFERENCE_INPUT}_$id"
    }

    companion object {
        private const val KEY_PREFERENCE_INPUT = "key_preference_input"
    }
}