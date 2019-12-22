package fr.geonature.commons.model

import android.os.Environment
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.NonNull
import fr.geonature.commons.BuildConfig
import fr.geonature.commons.util.DeviceUtils
import java.io.File
import java.io.IOException

/**
 * Describes a mount point storage.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MountPoint : Parcelable,
    Comparable<MountPoint> {

    val mountPath: File
    val storageType: StorageType

    constructor(
        mountPath: String,
        storageType: StorageType
    ) {
        var resolvedMountPath: String

        try {
            resolvedMountPath = File(mountPath).canonicalPath

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "MountPoint: '$mountPath', canonical path: '$resolvedMountPath'")
            }
        } catch (ioe: IOException) {
            resolvedMountPath = mountPath

            Log.w(TAG, "MountPoint: failed to get the canonical path of '$mountPath'")
        }

        this.mountPath = File(resolvedMountPath)
        this.storageType = storageType
    }

    private constructor(source: Parcel) {
        this.mountPath = source.readSerializable() as File
        this.storageType = source.readSerializable() as StorageType
    }

    @NonNull
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

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeSerializable(mountPath)
        dest.writeSerializable(storageType)
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
     * @author [S. Grimault](mailto:sebastien.grimault@makina-corpus.com)
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

    companion object {

        private val TAG = MountPoint::class.java.name

        @JvmField
        val CREATOR: Parcelable.Creator<MountPoint> = object : Parcelable.Creator<MountPoint> {

            override fun createFromParcel(source: Parcel): MountPoint {
                return MountPoint(source)
            }

            override fun newArray(size: Int): Array<MountPoint?> {
                return arrayOfNulls(size)
            }
        }
    }
}
