package fr.geonature.commons.util

import android.util.JsonReader
import android.util.JsonToken.STRING
import org.json.JSONArray
import org.json.JSONObject

/**
 * Utility functions about JSON.
 *
 * @author S. Grimault
 */

/**
 * Returns a [Map] representation of this [JSONObject].
 */
fun JSONObject.toMap(): Map<String, *> =
    keys()
        .asSequence()
        .associateWith {
            when (val value = this[it]) {
                is JSONArray -> {
                    JSONObject((0 until value.length()).associate { index ->
                        Pair(
                            index.toString(),
                            value[index]
                        )
                    }).toMap().values.toList()
                }
                is JSONObject -> value.toMap()
                JSONObject.NULL -> null
                else -> value
            }
        }

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
