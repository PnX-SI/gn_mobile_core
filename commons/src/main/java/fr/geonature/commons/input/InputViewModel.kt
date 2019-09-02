package fr.geonature.commons.input

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fr.geonature.commons.MainApplication
import fr.geonature.commons.input.io.InputJsonReader
import fr.geonature.commons.input.io.InputJsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * [AbstractInput] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
open class InputViewModel<I : AbstractInput>(application: MainApplication,
                                        inputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<I>,
                                        inputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<I>) : AndroidViewModel(application) {

    internal val inputManager = InputManager(application,
                                             inputJsonReaderListener,
                                             inputJsonWriterListener)

    private val inputsLiveData: MutableLiveData<List<I>> = MutableLiveData()

    /**
     * Reads all [AbstractInput]s.
     */
    fun readInputs(): LiveData<List<I>> {
        GlobalScope.launch(Dispatchers.Main) {
            inputsLiveData.postValue(inputManager.readInputs())
        }

        return inputsLiveData
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