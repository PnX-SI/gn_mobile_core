package fr.geonature.sync.util

import android.content.Context
import android.os.Environment
import android.util.Log
import fr.geonature.commons.model.MountPoint
import fr.geonature.commons.util.MountPointUtils
import java.io.File

/**
 * Helpers for [File] utilities.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object FileUtils {

    private val TAG = FileUtils::class.java.name

    /**
     * Construct a file from the set of name elements.
     *
     * @param directory the parent directory
     * @param names     the name elements
     *
     * @return the corresponding file
     */
    fun getFile(
        directory: File,
        vararg names: String): File {

        var file = directory

        for (name in names) {
            file = File(
                file,
                name)
        }

        return file
    }

    /**
     * Gets the relative path used by this context.
     *
     * @param context the current `Context`
     *
     * @return the relative path
     */
    fun getRelativeSharedPath(context: Context): String {

        return "Android" + File.separator + "data" + File.separator + context.packageName + File.separator
    }

    /**
     * Tries to find the mount point used by external storage as `File`.
     * If not, returns the default `Environment.getExternalStorageDirectory()`.
     *
     * @param context the current `Context`
     *
     * @return the mount point as `File` used by external storage if available
     */
    fun getExternalStorageDirectory(context: Context): File {

        val externalMountPoint = MountPointUtils.getExternalStorage(
            context,
            Environment.MEDIA_MOUNTED,
            Environment.MEDIA_MOUNTED_READ_ONLY)

        if (externalMountPoint == null) {
            Log.w(
                TAG,
                "getExternalStorageDirectory: external mount point is not available. Use default: " + MountPointUtils.getInternalStorage())

            return MountPointUtils.getInternalStorage().mountPath
        }

        return externalMountPoint.mountPath
    }

    /**
     * Gets the root folder as `File` used by this context.
     *
     * @param context     the current `Context`
     * @param storageType the [MountPoint.StorageType] to use
     *
     * @return the root folder as `File`
     */
    fun getRootFolder(
        context: Context,
        storageType: MountPoint.StorageType): File {

        return getFile(
            if (storageType === MountPoint.StorageType.EXTERNAL) getExternalStorageDirectory(context)
            else MountPointUtils.getInternalStorage().mountPath,
            getRelativeSharedPath(context))
    }

    /**
     * Gets the `inputs/` folder as `File` used by this context.
     * The relative path used is `inputs/<package_name>`
     *
     * @param context the current `Context`
     *
     * @return the `inputs/` folder as `File`
     */
    fun getInputsFolder(context: Context): File {

        return getFile(
            getRootFolder(
                context,
                MountPoint.StorageType.INTERNAL),
            "inputs",
            context.packageName)
    }

    /**
     * Gets the `databases/` folder as `File` used by this context.
     *
     * @param context     the current `Context`
     * @param storageType the [MountPoint.StorageType] to use
     *
     * @return the `databases/` folder
     */
    fun getDatabaseFolder(
        context: Context,
        storageType: MountPoint.StorageType): File {

        return getFile(
            getRootFolder(
                context,
                storageType),
            "databases")
    }
}