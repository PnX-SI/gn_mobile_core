package fr.geonature.commons.data.helper

/**
 * Helper about entity objects.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object EntityHelper {

    /**
     * Gets the full column name and its alias.
     */
    fun column(columnName: String,
               tableAlias: String? = null): Pair<String, String> {
        return Pair("${tableAlias.orEmpty()}${if (tableAlias.isNullOrBlank()) "" else "."}\"$columnName\"",
                    "${tableAlias.orEmpty()}${if (tableAlias.isNullOrBlank()) "" else "_"}$columnName")
    }
}