package fr.geonature.commons.input

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import fr.geonature.commons.data.helper.Provider
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.input.io.InputJsonWriter
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext

/**
 * Default implementation of [IInputManager].
 *
 * @author S. Grimault
 */
class InputManagerImpl<I : AbstractInput>(
    private val context: Context,
    inputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<I>,
    inputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<I>
) : IInputManager<I> {

    private val preferenceManager: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val inputJsonReader: InputJsonReader<I> = InputJsonReader(inputJsonReaderListener)
    private val inputJsonWriter: InputJsonWriter<I> = InputJsonWriter(inputJsonWriterListener)

    private val _inputs: MutableLiveData<List<I>> = MutableLiveData()
    override val inputs: LiveData<List<I>> = _inputs

    private val _input: MutableLiveData<I> = MutableLiveData()
    override val input: LiveData<I> = _input

    override suspend fun readInputs(): List<I> =
        withContext(Default) {
            preferenceManager.all.filterKeys { it.startsWith("${KEY_PREFERENCE_INPUT}_") }.values
                .mapNotNull { if (it is String && it.isNotBlank()) inputJsonReader.read(it) else null }
                .sortedBy { it.id }
                .also { _inputs.postValue(it) }
        }

    override suspend fun readInput(id: Long?): I? =
        withContext(Default) {
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

            inputJsonReader
                .read(inputAsJson)
                .also { _input.postValue(it) }
        }

    override suspend fun readCurrentInput(): I? {
        return readInput()
    }

    override suspend fun saveInput(input: I): Boolean {
        val saved = withContext(Default) {
            val inputAsJson = inputJsonWriter.write(input)
            if (inputAsJson.isNullOrBlank()) return@withContext false

            preferenceManager
                .edit()
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

        return saved && preferenceManager
            .contains(buildInputPreferenceKey(input.id))
            .also { readInputs() }
    }

    override suspend fun deleteInput(id: Long): Boolean {
        val deleted = withContext(Default) {
            preferenceManager
                .edit()
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

        _input.postValue(null)
        readInputs()

        return deleted && !preferenceManager.contains(buildInputPreferenceKey(id))
    }

    override suspend fun exportInput(id: Long): Boolean {
        val inputToExport = readInput(id)
            ?: return false

        return exportInput(inputToExport)
    }

    override suspend fun exportInput(input: I): Boolean {
        input.status = AbstractInput.Status.TO_SYNC

        val inputExportUri = Provider.buildUri(
            "inputs",
            "export"
        )

        val inputUri = kotlin
            .runCatching {
                context.contentResolver
                    .acquireContentProviderClient(inputExportUri)
                    ?.let {
                        val uri = it.insert(
                            inputExportUri,
                            toContentValues(input)
                        )

                        it.close()
                        uri
                    }
            }
            .getOrNull()

        if (inputUri == null) {
            input.status = AbstractInput.Status.DRAFT

            Log.w(
                TAG,
                "failed to export input '${input.id}'"
            )

            return false
        }

        Log.i(
            TAG,
            "input '${input.id}' exported (URI: $inputUri)"
        )

        return deleteInput(input.id)
    }

    private fun toContentValues(input: I): ContentValues {
        return ContentValues().apply {
            put(
                "id",
                input.id
            )
            put(
                "packageName",
                context.packageName
            )
            put(
                "data",
                inputJsonWriter.write(input)
            )
        }
    }

    private fun buildInputPreferenceKey(id: Long): String {
        return "${KEY_PREFERENCE_INPUT}_$id"
    }

    companion object {
        private val TAG = InputManagerImpl::class.java.name

        private const val KEY_PREFERENCE_INPUT = "key_preference_input"
        private const val KEY_PREFERENCE_CURRENT_INPUT = "key_preference_current_input"
    }
}
