package fr.geonature.mountpoint.util

import android.content.Context
import android.os.Environment
import android.util.Log
import fr.geonature.mountpoint.BuildConfig
import fr.geonature.mountpoint.R
import fr.geonature.mountpoint.model.MountPoint
import java.io.File
import java.io.FileNotFoundException
import java.util.ArrayList
import java.util.HashSet
import java.util.Scanner
import java.util.TreeSet

/**
 * Class helper about [MountPoint].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object MountPointUtils {

    private val TAG = MountPointUtils::class.java.name

    /**
     * Return the primary external storage as [MountPoint].
     *
     * @return the primary external storage
     */
    fun getInternalStorage(): MountPoint {
        val externalStorage = System.getenv("EXTERNAL_STORAGE")

        if (externalStorage.isNullOrBlank()) {
            val mountPoint = MountPoint(
                Environment.getExternalStorageDirectory().absolutePath,
                MountPoint.StorageType.INTERNAL
            )

            if (BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "internal storage from API: $mountPoint"
                )
            }

            return mountPoint
        }

        val mountPoint = MountPoint(
            externalStorage,
            MountPoint.StorageType.INTERNAL
        )

        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "internal storage from system environment: $mountPoint"
            )
        }

        return mountPoint
    }

    /**
     * Return the secondary external storage as [MountPoint] if found.
     *
     * @param context the current `Context`
     * @param storageStates a set of storage states as filter if [MountPoint.getStorageState]
     * matches at least one
     *
     * @return the secondary external storage or `null` if not found
     *
     * @see .getMountPoints
     */
    fun getExternalStorage(
        context: Context,
        vararg storageStates: String
    ): MountPoint? {
        val mountPoints = getMountPoints(context)
        val mountPointIterator = mountPoints.iterator()
        var externalMountPoint: MountPoint? = null

        while (mountPointIterator.hasNext() && externalMountPoint == null) {
            val mountPoint = mountPointIterator.next()
            val checkStorageState =
                storageStates.isEmpty() || listOf(*storageStates).contains(mountPoint.getStorageState())
            externalMountPoint =
                if (mountPoint.storageType == MountPoint.StorageType.EXTERNAL && checkStorageState) mountPoint
                else null
        }

        if (BuildConfig.DEBUG) {
            if (externalMountPoint == null) {
                Log.d(
                    TAG,
                    "external storage not found"
                )
            } else {
                Log.d(
                    TAG,
                    "external storage found: $externalMountPoint"
                )
            }
        }

        return externalMountPoint
    }

    /**
     * Retrieves a `List` of all available [MountPoint]s
     *
     * @param context the current `Context`
     *
     * @return a `List` of available [MountPoint]s
     *
     * @see .getMountPointsFromSystemEnv
     * @see .getMountPointsFromVold
     * @see .getMountPointsFromProcMounts
     */
    fun getMountPoints(context: Context): List<MountPoint> {
        // avoid duplicate mount points found
        val mountPoints = HashSet<MountPoint>()

        // first: add the primary external storage
        mountPoints.add(getInternalStorage())

        // then: add all externals storage found only from Android APIs if Android version >= 21
        if (DeviceUtils.isPostLollipop) {
            mountPoints.addAll(getMountPointsFromAPI(context))
        } else {
            // then: for all other Android versions try to find all MountPoints from System environment
            val mountPointsFromSystemEnv = mountPointsFromSystemEnv
            mountPoints.addAll(mountPointsFromSystemEnv)

            // fallback: try to find all externals storage from 'vold.fstab'
            if (mountPointsFromSystemEnv.isEmpty()) {
                val mountPointsFromVold = mountPointsFromVold
                val filteredMountPointsFromVold = ArrayList<MountPoint>()

                // keep only all secondary externals storage found
                for (mountPoint in mountPointsFromVold) {
                    if (mountPoint.storageType != MountPoint.StorageType.INTERNAL) {
                        filteredMountPointsFromVold.add(mountPoint)
                    }
                }

                mountPoints.addAll(filteredMountPointsFromVold)

                // fallback: try to find all externals storage from '/proc/mounts'
                if (filteredMountPointsFromVold.isEmpty()) {
                    val mountPointsFromProcMounts = mountPointsFromProcMounts
                    mountPoints.addAll(mountPointsFromProcMounts)

                    // fallback: try to find all externals storage from Android APIs if Android version >= 19
                    if (mountPointsFromProcMounts.isEmpty() && DeviceUtils.isPostKitKat) {
                        mountPoints.addAll(getMountPointsFromAPI(context))
                    }
                }
            }
        }

        // apply natural ordering using TreeSet
        return ArrayList(TreeSet(mountPoints))
    }

    /**
     * Check if the given [MountPoint] is mounted or not:
     *
     *  * `Environment.MEDIA_MOUNTED`
     *  * `Environment.MEDIA_MOUNTED_READ_ONLY`
     *
     * @param mountPoint the given [MountPoint] to check
     *
     * @return `true` if the given [MountPoint] is mounted, `false` otherwise
     */
    fun isMounted(mountPoint: MountPoint): Boolean {
        return mountPoint.getStorageState() == Environment.MEDIA_MOUNTED || mountPoint.getStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY
    }

    /**
     * Pretty format a storage size.
     *
     * @param context the current context
     * @param storageSize the storage size in bytes to format
     *
     * @return a human representation of the storage size
     */

    fun formatStorageSize(
        context: Context,
        storageSize: Long
    ): String {
        var storageSuffix = "b"
        var formattedStorageSize = storageSize.toFloat()

        if (formattedStorageSize >= 1024) {
            storageSuffix = "kb"
            formattedStorageSize /= 1024f

            if (formattedStorageSize >= 1024) {
                storageSuffix = "mb"
                formattedStorageSize /= 1024f

                if (formattedStorageSize >= 1024) {
                    storageSuffix = "gb"
                    formattedStorageSize /= 1024f
                }
            }
        }

        val stringResource = context.resources.getIdentifier(
            "storage_size_$storageSuffix",
            "string",
            context.packageName
        )

        return if (stringResource == 0) {
            context.getString(
                R.string.storage_size_kb,
                storageSize / 1024f
            )
        } else context.getString(
            stringResource,
            formattedStorageSize
        )
    }

    /**
     * Pretty format the storage status.
     *
     * @param context the current `Context`
     * @param status the storage status
     *
     * @return a human representation of the storage status
     */
    fun formatStorageStatus(
        context: Context,
        status: String
    ): String {
        val stringResource = context.resources.getIdentifier(
            "storage_status_$status",
            "string",
            context.packageName
        )

        return if (stringResource == 0) {
            context.getString(R.string.storage_status_unmounted)
        } else context.getString(stringResource)
    }

    /**
     * Retrieves a `List` of [MountPoint]s from Android APIs.
     *
     * @param context the current `Context`
     *
     * @return a `List` of available [MountPoint]s
     */
    private fun getMountPointsFromAPI(context: Context): List<MountPoint> {
        val mountPoints = ArrayList<MountPoint>()

        if (DeviceUtils.isPostKitKat) {
            val externalFilesDirs = context.getExternalFilesDirs(null)
            var firstPrimaryStorage = true

            for (file in externalFilesDirs) {
                if (file == null) {
                    continue
                }

                val path = file.absolutePath
                val mountPoint = buildMountPoint(
                    path.substring(
                        0,
                        path.indexOf("/Android").coerceAtLeast(0)
                    ),
                    if (firstPrimaryStorage) MountPoint.StorageType.INTERNAL else MountPoint.StorageType.EXTERNAL
                )

                if (mountPoint !== null) {
                    mountPoints.add(mountPoint)
                }

                firstPrimaryStorage = false
            }
        }

        return mountPoints
    }

    /**
     * Retrieves a `List` of [MountPoint]s from `System` environment.
     *
     * @return a `List` of available [MountPoint]s
     */
    private val mountPointsFromSystemEnv: List<MountPoint>
        get() {
            val mountPoints = ArrayList<MountPoint>()

            var secondaryStorage = System.getenv("SECONDARY_STORAGE")

            if (secondaryStorage.isNullOrBlank()) {
                secondaryStorage = System.getenv("EXTERNAL_SDCARD_STORAGE")
            }

            if (!secondaryStorage.isNullOrBlank()) {
                val paths = secondaryStorage.split(":".toRegex())
                    .dropLastWhile {
                        it.isEmpty()
                    }
                    .toTypedArray()
                var firstSecondaryStorage = true

                for (path in paths) {
                    val mountPoint = buildMountPoint(
                        path,
                        if (firstSecondaryStorage) MountPoint.StorageType.EXTERNAL else MountPoint.StorageType.USB
                    )

                    if (mountPoint != null) {
                        if (BuildConfig.DEBUG) {
                            Log.d(
                                TAG,
                                "mount point found from system environment: $mountPoint"
                            )
                        }

                        mountPoints.add(mountPoint)
                        firstSecondaryStorage = false
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "mount points found from system environment: " + mountPoints.size
                )
            }

            return mountPoints
        }

    /**
     * Retrieves a `List` of [MountPoint]s from `'vold.fstab'` system file.
     *
     * @return a `List` of available [MountPoint]s
     */
    private val mountPointsFromVold: List<MountPoint>
        get() {
            val mountPoints = ArrayList<MountPoint>()

            try {
                val scanner = Scanner(File("/system/etc/vold.fstab"))

                while (scanner.hasNext()) {
                    var line = scanner.nextLine()

                    if (line.isNullOrBlank()) {
                        continue
                    }

                    line = line.trim { it <= ' ' }

                    var storageType: MountPoint.StorageType? = null

                    // parse line comment
                    if (line.startsWith("#")) {
                        storageType = when {
                            line.contains("internal") -> MountPoint.StorageType.INTERNAL
                            line.contains("external") -> MountPoint.StorageType.EXTERNAL
                            line.contains("usb") -> MountPoint.StorageType.USB
                            else -> // storage type not found from line comment. Continue anyway
                                null
                        }
                    }

                    // parse 'media_type' only it the storage type was not found from line comment
                    if (line.startsWith("media_type") && storageType == null) {
                        val tokens = line.split("\\s".toRegex())
                            .dropLastWhile {
                                it.isEmpty()
                            }
                            .toTypedArray()

                        if (tokens.size == 3) {
                            if (tokens[2].contains("usb")) {
                                storageType = MountPoint.StorageType.USB
                            }
                        }
                    }

                    // parse 'dev_mount'
                    if (line.startsWith("dev_mount") && storageType != null) {
                        val tokens = line.split("\\s".toRegex())
                            .dropLastWhile {
                                it.isEmpty()
                            }
                            .toTypedArray()

                        if (tokens.size >= 3) {
                            val mountPoint = buildMountPoint(
                                tokens[2],
                                storageType
                            )

                            if (mountPoint != null) {
                                mountPoints.add(mountPoint)
                            }
                        }
                    }
                }

                scanner.close()
            } catch (fnfe: FileNotFoundException) {
                Log.w(
                    TAG,
                    fnfe.message
                )
            }

            if (BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "mount points found from 'vold.fstab': " + mountPoints.size
                )
            }

            return mountPoints
        }

    /**
     * Retrieves a `List` of [MountPoint]s from `'/proc/mounts'` system file.
     *
     * @return a `List` of available [MountPoint]s
     */
    private val mountPointsFromProcMounts: List<MountPoint>
        get() {
            val mountPoints = ArrayList<MountPoint>()

            try {
                val scanner = Scanner(File("/proc/mounts"))

                while (scanner.hasNext()) {
                    val line = scanner.nextLine()

                    if (line.startsWith("/dev/block/vold") || line.startsWith("/dev/fuse")) {
                        val tokens = line.split("\\s".toRegex())
                            .dropLastWhile {
                                it.isEmpty()
                            }
                            .toTypedArray()

                        if (tokens.size >= 2) {
                            val mountPoint = buildMountPoint(
                                tokens[1],
                                MountPoint.StorageType.EXTERNAL
                            )

                            if (mountPoint != null) {
                                mountPoints.add(mountPoint)
                            }
                        }
                    }
                }

                scanner.close()
            } catch (fnfe: FileNotFoundException) {
                Log.w(
                    TAG,
                    fnfe.message
                )
            }

            if (BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "mount points found from '/proc/mounts': " + mountPoints.size
                )
            }

            return mountPoints
        }

    private fun buildMountPoint(
        mountPath: File,
        storageType: MountPoint.StorageType
    ): MountPoint? {
        if (!mountPath.isDirectory) {
            if (BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "failed to build mount point from '$mountPath'"
                )
            }

            return null
        }

        return MountPoint(
            mountPath.absolutePath,
            storageType
        )
    }

    private fun buildMountPoint(
        mountPath: String,
        storageType: MountPoint.StorageType
    ): MountPoint? {
        return buildMountPoint(
            File(if (mountPath.isBlank()) "/" else mountPath),
            storageType
        )
    }
}
