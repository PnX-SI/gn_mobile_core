package fr.geonature.mountpoint.model

import android.os.Environment
import android.os.Parcelable
import fr.geonature.mountpoint.util.DeviceUtils
import kotlinx.parcelize.Parcelize
import java.io.File

/**
 * Describes a mount point storage.
 *
 * @author S. Grimault
 */
@Parcelize
data class MountPoint(
    val mountPath: File,
    val storageType: StorageType
) : Parcelable, Comparable<MountPoint> {

    init {
        require(mountPath.isDirectory)
    }

    fun getStorageState(): String {

        if (DeviceUtils.isPostLollipop) {
            return Environment.getExternalStorageState(mountPath)
        }

        var storageState = Environment.MEDIA_UNMOUNTED

        if (mountPath.canWrite()) {
            storageState = Environment.MEDIA_MOUNTED
        } else if (mountPath.canRead()) {
            storageState = Environment.MEDIA_MOUNTED_READ_ONLY
        }

        return storageState
    }

    override fun compareTo(other: MountPoint): Int {
        return if (storageType == other.storageType) {
            mountPath.compareTo(other.mountPath)
        } else storageType.compareTo(other.storageType)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val that = other as MountPoint?

        return mountPath == that!!.mountPath
    }

    override fun hashCode(): Int {
        return mountPath.hashCode()
    }

    override fun toString(): String {
        return "MountPoint(mountPath=$mountPath, storageType=$storageType)"
    }

    /**
     * Describes a storage type.
     *
     * @author S. Grimault
     */
    enum class StorageType {

        /**
         * Internal storage.
         */
        INTERNAL,

        /**
         * External storage.
         */
        EXTERNAL,

        /**
         * USB storage.
         */
        USB
    }
}
