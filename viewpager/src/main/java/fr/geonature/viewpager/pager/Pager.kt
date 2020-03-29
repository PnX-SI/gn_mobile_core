package fr.geonature.viewpager.pager

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayDeque
import java.util.Deque

/**
 * Describes a Pager metadata:
 *
 *  * the Pager size
 *  * the Pager current position
 *  * the Pager navigation history if any
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class Pager : Parcelable {

    var id: Long = 0
    var size: Int = 0
    var position: Int = 0
    val history: Deque<Int> = ArrayDeque()

    constructor(id: Long = 0) {
        this.id = id
    }

    private constructor(source: Parcel) {
        id = source.readLong()
        size = source.readInt()
        position = source.readInt()

        val navigationHistoryList = IntArray(source.readInt())
        source.readIntArray(navigationHistoryList)
        history.addAll(navigationHistoryList.asList())
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeLong(id)
        dest.writeInt(size)
        dest.writeInt(position)
        dest.writeInt(history.size)
        dest.writeIntArray(history.toIntArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null) {
            return false
        }

        if (javaClass != other.javaClass) {
            return false
        }

        val pager = other as Pager

        if (id != pager.id) {
            return false
        }

        if (size != pager.size) {
            return false
        }

        return if (position != pager.position) {
            false
        } else history.toTypedArray()
            .contentEquals(pager.history.toTypedArray())
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + size
        result = 31 * result + position
        result = 31 * result + history.hashCode()

        return result
    }

    override fun toString(): String {
        return "Pager(id=$id, size=$size, position=$position, history=$history)"
    }

    companion object CREATOR : Parcelable.Creator<Pager> {
        override fun createFromParcel(source: Parcel): Pager {
            return Pager(source)
        }

        override fun newArray(size: Int): Array<Pager?> {
            return arrayOfNulls(size)
        }
    }
}
