package fr.geonature.commons.input

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.util.IsoDateUtils.toDate
import java.util.ArrayList
import java.util.Date
import java.util.TreeMap

/**
 * Describes a current input.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
abstract class AbstractInput(

    /**
     * The module type of this AbstractInput.
     */
    var module: String) : Parcelable {

    var id: Long = 0
    var date: Date = Date()
    private val inputObserverIds: MutableSet<Long> = mutableSetOf()
    private val inputTaxa: MutableMap<Long, AbstractInputTaxon> = TreeMap()

    constructor(source: Parcel) : this(source.readString()!!) {
        this.id = source.readLong()
        this.date = source.readSerializable() as Date

        val inputObserverId = source.readLong()

        if (inputObserverId > 0) {
            this.inputObserverIds.add(inputObserverId)
        }

        val longArray = source.createLongArray()

        if (longArray != null) {
            this.inputObserverIds.addAll(longArray.toList())
        }

        this.setInputTaxa(this.getTaxaFromParcel(source))
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel,
                               flags: Int) {
        dest.writeString(module)
        dest.writeLong(this.id)
        dest.writeSerializable(this.date)
        dest.writeLong(if (inputObserverIds.isEmpty()) -1 else inputObserverIds.first())
        dest.writeLongArray(inputObserverIds.drop(1).toLongArray())
        dest.writeTypedList(getInputTaxa())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractInput

        if (module != other.module) return false
        if (id != other.id) return false
        if (date != other.date) return false
        if (inputObserverIds != other.inputObserverIds) return false
        if (inputTaxa != other.inputTaxa) return false

        return true
    }

    override fun hashCode(): Int {
        var result = module.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + inputObserverIds.hashCode()
        result = 31 * result + inputTaxa.hashCode()

        return result
    }

    fun setDate(isoDate: String?) {
        val date = toDate(isoDate)
        this.date = date ?: Date()
    }

    fun getInputObserverIds(): Set<Long> {
        return this.inputObserverIds
    }

    fun clearInputObservers() {
        this.inputObserverIds.clear()
    }

    fun setPrimaryInputObserver(inputObserver: InputObserver) {
        val inputObservers = this.inputObserverIds.toMutableList()
            .apply {
                add(0,
                    inputObserver.id)
            }
        this.inputObserverIds.clear()
        this.inputObserverIds.addAll(inputObservers)
    }

    fun setInputObservers(inputObservers: List<InputObserver>) {
        val primaryInputObserver = this.inputObserverIds.firstOrNull()

        this.inputObserverIds.clear()

        if (primaryInputObserver != null) {
            this.inputObserverIds.add(primaryInputObserver)
        }

        this.inputObserverIds.addAll(inputObservers.map { inputObserver -> inputObserver.id })
    }

    fun getInputTaxa(): List<AbstractInputTaxon> {
        return ArrayList(this.inputTaxa.values)
    }

    fun setInputTaxa(inputTaxa: List<AbstractInputTaxon>) {
        this.inputTaxa.clear()

        for (inputTaxon in inputTaxa) {
            this.inputTaxa[inputTaxon.id] = inputTaxon
        }
    }

    abstract fun getTaxaFromParcel(source: Parcel): List<AbstractInputTaxon>
}
