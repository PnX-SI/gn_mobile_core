package fr.geonature.commons.util

import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonToken.NAME
import android.util.JsonToken.STRING
import java.io.IOException

/**
 * Utility functions about JsonReader.
 *
 * @author S. Grimault
 */

/**
 * Returns the next token, a property name, consumes it and check if its value matches the given `JsonToken`.
 */
fun JsonReader.nextName(name: String, expected: JsonToken): JsonReader {
    return if (peek() == NAME) {
        val nextName = nextName()

        if (nextName != name) {
            throw IOException("Expected a property name '$name' but was '${peek().name}'")
        }

        if (peek() != expected) {
            throw IOException("Expected a property name '$name' to be '${expected.name}' but was '${peek().name}'")
        }

        this
    } else {
        throw IOException("Missing '$name' property")
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
            null
        }
    }
}
