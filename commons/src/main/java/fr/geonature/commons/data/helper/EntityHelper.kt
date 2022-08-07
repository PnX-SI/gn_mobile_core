package fr.geonature.commons.data.helper

/**
 * Helper about entity objects.
 *
 * @author S. Grimault
 */
object EntityHelper {

    /**
     * Normalize query string.
     */
    val normalize: (String) -> String = { input ->
        input
            .lowercase()
            .asSequence()
            .map {
                when (it) {
                    'a', 'á', 'à', 'ä', 'â', 'ã', 'A', 'Á', 'À', 'Ä', 'Â', 'Ã' -> "[aáàäâãAÁÀÄÂÃ]"
                    'b', 'B' -> "[bB]"
                    'c', 'ç', 'C', 'Ç' -> "[cçCÇ]"
                    'd', 'D' -> "[dD]"
                    'e', 'é', 'è', 'ë', 'ê', 'ẽ', 'E', 'É', 'È', 'Ë', 'Ê', 'Ẽ' -> "[eéèëêẽEÉÈËÊẼ]"
                    'f', 'F' -> "[fF]"
                    'g', 'G' -> "[gG]"
                    'h', 'H' -> "[hH]"
                    'i', 'í', 'ì', 'ï', 'î', 'ĩ', 'I', 'Í', 'Ì', 'Ï', 'Î', 'Ĩ' -> "[iíìïîĩIÍÌÏÎĨ]"
                    'j', 'J' -> "[jJ]"
                    'k', 'K' -> "[kK]"
                    'l', 'L' -> "[lL]"
                    'm', 'M' -> "[mM]"
                    'n', 'ñ', 'N', 'Ñ' -> "[nñNÑ]"
                    'o', 'ó', 'ò', 'ö', 'ô', 'õ', 'O', 'Ó', 'Ò', 'Ö', 'Ô', 'Õ' -> "[oóòöôõõOÓÒÖÔÕ]"
                    'p', 'P' -> "[pP]"
                    'q', 'Q' -> "[qQ]"
                    'r', 'R' -> "[rR]"
                    's', 'S' -> "[sS]"
                    't', 'T' -> "[tT]"
                    'u', 'ú', 'ù', 'ü', 'û', 'ũ', 'U', 'Ú', 'Ù', 'Ü', 'Û', 'Ũ' -> "[uúùüûũUÚÙÜÛŨ]"
                    'v', 'V' -> "[vV]"
                    'w', 'W' -> "[wW]"
                    'x', 'X' -> "[xX]"
                    'y', 'Y' -> "[yY]"
                    'z', 'Z' -> "[zZ]"
                    else -> it
                }
            }
            .joinToString(
                separator = "",
                prefix = "*",
                postfix = "*"
            )
    }

    /**
     * Gets the full column name and its alias.
     */
    fun column(
        columnName: String,
        tableAlias: String? = null
    ): Pair<String, String> {
        return Pair(
            "${tableAlias.orEmpty()}${if (tableAlias.isNullOrBlank()) "" else "."}\"$columnName\"",
            "${tableAlias.orEmpty()}${if (tableAlias.isNullOrBlank()) "" else "_"}$columnName"
        )
    }
}
