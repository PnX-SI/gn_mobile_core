package fr.geonature.commons.util

/**
 * String helper.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object StringUtils {
    fun isEmpty(str: String?): Boolean {
        return str == null || str.isEmpty()
    }
}