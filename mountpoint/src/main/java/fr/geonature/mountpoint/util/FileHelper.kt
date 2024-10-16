package fr.geonature.mountpoint.util

import java.io.File

/**
 * Function helpers for `File`.
 *
 * @author S. Grimault
 */

/**
 * Construct a file from the set of name elements using the current file as parent.
 *
 * @param names the name elements
 *
 * @return the corresponding file
 */
fun File.getFile(vararg names: String): File {
    var file = this

    for (name in names) {
        file = File(
            file,
            name
        )
    }

    return file.also { it.parentFile?.mkdirs() }
}