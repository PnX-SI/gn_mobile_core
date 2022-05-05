package fr.geonature.commons.util

import android.content.Context
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.FileUtils
import java.io.File

/**
 * Function helpers for `File`.
 *
 * @author S. Grimault
 */

/**
 * Gets the `inputs/` folder as `File` from given package ID.
 * If package ID is `null` use given context.
 * The relative path used is `inputs/<package_name>`
 *
 * @param context the current `Context`
 * @param packageId the package ID (may be `null`)
 *
 * @return the `inputs/` folder as `File`
 */
fun FileUtils.getInputsFolder(
    context: Context,
    packageId: String? = null
): File {

    return getFile(
        getRootFolder(
            context,
            MountPoint.StorageType.INTERNAL,
            packageId
        ),
        "inputs"
    )
}

/**
 * Gets the `databases/` folder as `File` used by this context.
 *
 * @param context the current `Context`
 * @param storageType the [MountPoint.StorageType] to use
 *
 * @return the `databases/` folder
 */
fun FileUtils.getDatabaseFolder(
    context: Context,
    storageType: MountPoint.StorageType
): File {

    return getFile(
        getRootFolder(
            context,
            storageType
        ),
        "databases"
    )
}
