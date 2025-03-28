package fr.geonature.mountpoint.util

import java.io.File

/**
 * Function helpers for `File`.
 *
 * @author S. Grimault
 */

/**
 * Construct a file from the set of name elements using the current file as parent. This method
 * creates given parent paths if not exists.
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

/**
 * Tries to find recursively the first file matching the given search path.
 */
fun File.find(searchPath: String): File? {
    // return null if the given search path is empty...
    if (searchPath.isEmpty() || searchPath.isBlank()) return null

    // if the search path is absolute, check if it matches an existing file and return it directly
    if (searchPath.startsWith(File.separator)) return File(searchPath).takeIf { it.exists() }

    // checks if the current file path matches the given search path
    if (absolutePath.endsWith(searchPath)) return this

    // checks for all files within this directory
    listFiles()?.forEach { file ->
        file.find(searchPath)
            ?.let { return it }
    }

    return null
}

