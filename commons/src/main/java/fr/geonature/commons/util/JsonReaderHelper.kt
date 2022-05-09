package fr.geonature.commons.util

import android.util.JsonReader
import android.util.JsonToken.STRING

/**
 * Utility functions about JsonReader.
 *
 * @author S. Grimault
 */

/**
 * Returns the string value of the next token and consuming it.
 * If the next token is not a string returns `null`.
 */
fun JsonReader.nextStringOrNull(): String? {
    return when (peek()) {
        STRING -> {
            nextString()
        }
        else -> {
            skipValue()
            null
        }
    }
}
