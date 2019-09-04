package fr.geonature.commons.input

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.input.io.InputJsonWriter
import kotlinx.coroutines.launch

/**
 * [AbstractInput] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
open class InputViewModel<I : AbstractInput>(application: Application,
                                             inputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<I>,
                                             inputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<I>) : AndroidViewModel(application) {

    internal val inputManager = InputManager(application,
                                             inputJsonReaderListener,
                                             inputJsonWriterListener)

    private val inputsLiveData: MutableLiveData<List<I>> = MutableLiveData()
    private var selectedInputToDelete: I? = null

    /**
     * Reads all [AbstractInput]s.
     */
    fun readInputs(): LiveData<List<I>> {
        viewModelScope.launch {
            inputsLiveData.postValue(inputManager.readInputs())
        }

        return inputsLiveData
    }

    /**
     * Saves the given [AbstractInput] and sets it as default current [AbstractInput].
     */
    fun saveInput(input: I) {
        viewModelScope.launch {
            val saved = inputManager.saveInput(input)

            if (saved) {
                inputsLiveData.postValue(inputManager.readInputs())
            }
        }
    }

    /**
     * Deletes [AbstractInput] from given ID.
     *
     * @param input the [AbstractInput] to delete
     */
    fun deleteInput(input: I) {
        viewModelScope.launch {
            val deleted = inputManager.deleteInput(input.id)

            if (deleted) {
                selectedInputToDelete = input
                inputsLiveData.postValue(inputManager.readInputs())
            }
        }
    }

    /**
     * Restores previously deleted [AbstractInput].
     */
    fun restoreDeletedInput() {
        val selectedInputToRestore = selectedInputToDelete ?: return

        viewModelScope.launch {
            inputManager.saveInput(selectedInputToRestore)
            inputsLiveData.postValue(inputManager.readInputs())
            selectedInputToDelete = null
        }
    }

    /**
     * Default Factory to use for [InputViewModel].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    class Factory<T : InputViewModel<I>, I : AbstractInput>(val creator: () -> T) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }
}