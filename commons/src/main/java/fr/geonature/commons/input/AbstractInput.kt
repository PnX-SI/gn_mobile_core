package fr.geonature.commons.input

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.util.IsoDateUtils.toDate
import java.util.ArrayList
import java.util.Calendar
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

    var id: Long = generateId()
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

    /**
     * Gets the primary input observer.
     */
    fun getPrimaryObserverId(): Long? {
        return this.inputObserverIds.firstOrNull()
    }

    /**
     * Gets all input observers (i.e. the primary input observer at first position, then others).
     */
    fun getAllInputObserverIds(): Set<Long> {
        return this.inputObserverIds
    }

    /**
     * Gets only selected input observers without the primary input observer.
     */
    fun getInputObserverIds(): Set<Long> {
        return this.inputObserverIds.drop(1)
            .toSet()
    }

    fun clearAllInputObservers() {
        this.inputObserverIds.clear()
    }

    fun setPrimaryInputObserverId(id: Long) {
        val inputObservers = this.inputObserverIds.toMutableList()
            .apply {
                add(0,
                    id)
            }
        this.inputObserverIds.clear()
        this.inputObserverIds.addAll(inputObservers)
    }

    fun setPrimaryInputObserver(inputObserver: InputObserver) {
        setPrimaryInputObserverId(inputObserver.id)
    }

    fun setAllInputObservers(inputObservers: List<InputObserver>) {
        val primaryInputObserver = this.inputObserverIds.firstOrNull()

        this.inputObserverIds.clear()

        if (primaryInputObserver != null) {
            this.inputObserverIds.add(primaryInputObserver)
        }

        this.inputObserverIds.addAll(inputObservers.map { inputObserver -> inputObserver.id })
    }

    fun addInputObserverId(id: Long) {
        this.inputObserverIds.add(id)
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

    fun addInputTaxon(inputTaxon: AbstractInputTaxon) {
        this.inputTaxa[inputTaxon.id] = inputTaxon
    }

    abstract fun getTaxaFromParcel(source: Parcel): List<AbstractInputTaxon>

    /**
     * Generates a pseudo unique ID. The value is the number of seconds since Jan. 1, 2016, midnight.
     *
     * @return an unique ID
     */
    private fun generateId(): Long {
        val now = Calendar.getInstance()
        now.set(Calendar.MILLISECOND,
                0)

        val start = Calendar.getInstance()
        start.set(2016,
                  Calendar.JANUARY,
                  1,
                  0,
                  0,
                  0)
        start.set(Calendar.MILLISECOND,
                  0)

        return (now.timeInMillis - start.timeInMillis) / 1000
    }
}
