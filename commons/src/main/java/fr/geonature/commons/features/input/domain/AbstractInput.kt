package fr.geonature.commons.features.input.domain

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.commons.data.entity.InputObserver
import java.util.Calendar
import java.util.Date

/**
 * Describes a current input.
 *
 * @author S. Grimault
 */
abstract class AbstractInput(

    /**
     * The module type of this AbstractInput.
     */
    var module: String
) : Parcelable {

    var id: Long = generateId()
    var startDate: Date = Date()
        set(value) {
            field = value
            if (endDate.before(field)) {
                endDate = field
            }
        }
    var endDate: Date = startDate
        set(value) {
            field = value
            if (startDate.after(field)) {
                startDate = field
            }
        }
    var status: Status = Status.DRAFT
    var datasetId: Long? = null
    private val inputObserverIds: MutableSet<Long> = mutableSetOf()
    private val inputTaxa: MutableMap<Long, AbstractInputTaxon> = LinkedHashMap()
    private var currentSelectedInputTaxonId: Long? = null

    constructor(source: Parcel) : this(source.readString()!!) {
        this.id = source.readLong()
        this.startDate = source.readSerializable() as Date
        this.endDate = source.readSerializable() as Date
        this.status = source
            .readString()
            .let { statusAsString ->
                Status
                    .values()
                    .firstOrNull { it.name == statusAsString }
                    ?: Status.DRAFT
            }
        this.datasetId = source
            .readLong()
            .takeIf { it != -1L }

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

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeString(module)
            it.writeLong(this.id)
            it.writeSerializable(this.startDate)
            it.writeSerializable(this.endDate)
            it.writeString(this.status.name)
            it.writeLong(
                this.datasetId
                    ?: -1L
            )
            it.writeLong(if (inputObserverIds.isEmpty()) -1 else inputObserverIds.first())
            it.writeLongArray(
                inputObserverIds
                    .drop(1)
                    .toLongArray()
            )
            it.writeTypedList(getInputTaxa())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AbstractInput

        if (module != other.module) return false
        if (id != other.id) return false
        if (startDate != other.startDate) return false
        if (endDate != other.endDate) return false
        if (status != other.status) return false
        if (datasetId != other.datasetId) return false
        if (inputObserverIds != other.inputObserverIds) return false
        if (inputTaxa != other.inputTaxa) return false

        return true
    }

    override fun hashCode(): Int {
        var result = module.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + startDate.hashCode()
        result = 31 * result + endDate.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + datasetId.hashCode()
        result = 31 * result + inputObserverIds.hashCode()
        result = 31 * result + inputTaxa.hashCode()

        return result
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
        return this.inputObserverIds
            .drop(1)
            .toSet()
    }

    fun clearAllInputObservers() {
        this.inputObserverIds.clear()
    }

    fun setPrimaryInputObserverId(id: Long) {
        val inputObservers = this.inputObserverIds
            .toMutableList()
            .apply {
                add(
                    0,
                    id
                )
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
            this.inputTaxa[inputTaxon.taxon.id] = inputTaxon
        }
    }

    fun addInputTaxon(inputTaxon: AbstractInputTaxon) {
        this.inputTaxa[inputTaxon.taxon.id] = inputTaxon
        this.currentSelectedInputTaxonId = inputTaxon.taxon.id
    }

    fun removeInputTaxon(inputTaxonId: Long) {
        this.inputTaxa.remove(inputTaxonId)

        if (this.currentSelectedInputTaxonId == inputTaxonId) {
            this.currentSelectedInputTaxonId = null
        }
    }

    fun getCurrentSelectedInputTaxon(): AbstractInputTaxon? {
        return this.inputTaxa[this.currentSelectedInputTaxonId]
    }

    fun setCurrentSelectedInputTaxonId(inputTaxonId: Long?) {
        this.currentSelectedInputTaxonId = inputTaxonId
    }

    fun clearCurrentSelectedInputTaxon() {
        this.currentSelectedInputTaxonId = null
    }

    fun getLastAddedInputTaxon(): AbstractInputTaxon? {
        if (this.inputTaxa.isEmpty()) {
            return null
        }

        return this.inputTaxa[this.inputTaxa.keys.last()]
    }

    abstract fun getTaxaFromParcel(source: Parcel): List<AbstractInputTaxon>

    enum class Status {
        DRAFT,
        TO_SYNC
    }

    /**
     * Generates a pseudo unique ID. The value is the number of seconds since Jan. 1, 2016, midnight.
     *
     * @return an unique ID
     */
    private fun generateId(): Long {
        val now = Calendar.getInstance()
        now.set(
            Calendar.MILLISECOND,
            0
        )

        val start = Calendar.getInstance()
        start.set(
            2016,
            Calendar.JANUARY,
            1,
            0,
            0,
            0
        )
        start.set(
            Calendar.MILLISECOND,
            0
        )

        return (now.timeInMillis - start.timeInMillis) / 1000
    }
}
