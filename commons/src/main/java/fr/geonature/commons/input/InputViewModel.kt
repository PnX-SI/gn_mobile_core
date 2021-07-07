package fr.geonature.commons.input

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [AbstractInput] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
open class InputViewModel<I : AbstractInput>(private val inputManager: IInputManager<I>) :
    ViewModel() {

    private var deletedInputToRestore: I? = null

    /**
     * Reads all [AbstractInput]s.
     */
    fun readInputs(): LiveData<List<I>> {
        viewModelScope.launch {
            inputManager.readInputs()
        }

        return inputManager.inputs
    }

    /**
     * Reads [AbstractInput] from given ID.
     *
     * @param id The [AbstractInput] ID to read. If omitted, read the current saved [AbstractInput].
     */
    open fun readInput(id: Long? = null): LiveData<I> {
        viewModelScope.launch {
            inputManager.readInput(id)
        }

        return inputManager.input
    }

    /**
     * Reads the current [AbstractInput].
     */
    fun readCurrentInput(): LiveData<I> {
        return readInput()
    }

    /**
     * Saves the given [AbstractInput] and sets it as default current [AbstractInput].
     *
     * @param input the [AbstractInput] to save
     */
    fun saveInput(input: I) {
        GlobalScope.launch(Main) {
            inputManager.saveInput(input)
        }
    }

    /**
     * Deletes [AbstractInput] from given ID.
     *
     * @param input the [AbstractInput] to delete
     */
    fun deleteInput(input: I) {
        viewModelScope.launch {
            if (inputManager.deleteInput(input.id)) {
                deletedInputToRestore = input
            }
        }
    }

    /**
     * Restores previously deleted [AbstractInput].
     */
    fun restoreDeletedInput() {
        val selectedInputToRestore = deletedInputToRestore
            ?: return

        viewModelScope.launch {
            inputManager.saveInput(selectedInputToRestore)
            deletedInputToRestore = null
        }
    }

    /**
     * Exports [AbstractInput] from given ID as `JSON` file.
     *
     * @param id the [AbstractInput] ID to export
     */
    fun exportInput(
        id: Long,
        exported: () -> Unit = {}
    ) {
        GlobalScope.launch(Main) {
            inputManager
                .exportInput(id)
                .also {
                    if (it) {
                        exported()
                    }
                }
        }
    }

    /**
     * Exports given [AbstractInput] as `JSON` file.
     *
     * @param input the [AbstractInput] to export
     */
    fun exportInput(
        input: I,
        exported: () -> Unit = {}
    ) {
        GlobalScope.launch(Main) {
            inputManager
                .exportInput(input)
                .also {
                    if (it) {
                        exported()
                    }
                }
        }
    }

    /**
     * Default Factory to use for [InputViewModel].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    class Factory<T : InputViewModel<I>, I : AbstractInput>(val creator: () -> T) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }
}
