package fr.geonature.commons.features.input.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.usecase.DeleteInputUseCase
import fr.geonature.commons.features.input.usecase.ExportInputUseCase
import fr.geonature.commons.features.input.usecase.ReadInputsUseCase
import fr.geonature.commons.features.input.usecase.SaveInputUseCase
import fr.geonature.commons.interactor.BaseUseCase
import fr.geonature.commons.lifecycle.BaseViewModel
import fr.geonature.commons.settings.IAppSettings

/**
 * [AbstractInput] view model.
 *
 * @author S. Grimault
 */
open class InputViewModel<I : AbstractInput, S : IAppSettings>(
    private val readInputsUseCase: ReadInputsUseCase<I, S>,
    private val saveInputUseCase: SaveInputUseCase<I, S>,
    private val deleteInputUseCase: DeleteInputUseCase<I, S>,
    private val exportInputUseCase: ExportInputUseCase<I, S>
) : BaseViewModel() {

    private val _inputs = MutableLiveData<List<I>>()
    val inputs: LiveData<List<I>> = _inputs

    private val _input = MutableLiveData<I>()
    val input: LiveData<I> = _input

    /**
     * Reads all [AbstractInput]s.
     *
     * @param filter additional filter to apply
     */
    fun readInputs(filter: (input: AbstractInput) -> Boolean = { true }) {
        readInputsUseCase(
            BaseUseCase.None(),
            viewModelScope
        ) {
            it.fold(::handleFailure) { inputs ->
                _inputs.value = inputs.filter { input -> filter(input) }
            }
        }
    }

    /**
     * Edit current input.
     *
     * @param input the [AbstractInput] to edit
     */
    fun editInput(input: I) {
        _input.value = input
    }

    /**
     * Saves the given [AbstractInput].
     *
     * @param input the [AbstractInput] to save
     */
    fun saveInput(input: I) {
        saveInputUseCase(
            SaveInputUseCase.Params(input),
            viewModelScope
        ) {
            it.fold(::handleFailure) { inputSaved ->
                _input.value = inputSaved
                _inputs.value =
                    (_inputs.value?.filter { existingInput -> existingInput.id != inputSaved.id }
                        ?: emptyList()) + listOf(inputSaved)
            }
        }
    }

    /**
     * Deletes [AbstractInput] from given ID.
     *
     * @param input the [AbstractInput] to delete
     */
    fun deleteInput(input: I) {
        deleteInputUseCase(
            DeleteInputUseCase.Params(input),
            viewModelScope
        ) {
            it.fold(::handleFailure) {
                _inputs.value =
                    _inputs.value?.filter { existingInput -> existingInput.id != input.id }
            }
        }
    }

    /**
     * Exports [AbstractInput] as `JSON` file.
     *
     * @param input the [AbstractInput] to save
     * @param settings additional settings
     */
    fun exportInput(
        input: I,
        settings: S? = null,
        exported: () -> Unit = {}
    ) {
        exportInputUseCase(
            ExportInputUseCase.Params(
                input,
                settings
            ),
            viewModelScope
        ) {
            it.fold(::handleFailure) { inputExported ->
                _input.value = inputExported
                _inputs.value =
                    (_inputs.value?.filter { existingInput -> existingInput.id != inputExported.id }
                        ?: emptyList()) + listOf(inputExported)

                exported()
            }
        }
    }
}