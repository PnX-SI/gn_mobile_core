package fr.geonature.commons.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.TimeZone.getTimeZone

/**
 * `Date` helpers.
 *
 * @author S. Grimault
 */

/**
 * Tries to parse given string to [Date].
 */
fun toDate(str: String?): Date? {
    if (str.isNullOrBlank()) return null

    val parse: (String) -> Date? = {
        runCatching {
            SimpleDateFormat(
                it,
                Locale.getDefault()
            )
                .apply { timeZone = getTimeZone("UTC") }
                .parse(str)
        }.getOrNull()
    }

    return parse("yyyy-MM-dd'T'HH:mm:ss'Z'")
        ?: parse("yyyy-MM-dd")
        ?: parse("HH:mm")
}

/**
 * Formats current date using the given pattern.
 */
fun Date.format(
    pattern: String,
    timeZone: TimeZone = getTimeZone("UTC")
): String {
    return SimpleDateFormat(
        pattern,
        Locale.getDefault()
    )
        .apply { this.timeZone = timeZone }
        .format(this)
}

/**
 * Formats current date to ISO-8601.
 */
fun Date.toIsoDateString(): String {
    return format("yyyy-MM-dd'T'HH:mm:ss'Z'")
}

/**
 * Returns the value of the given calendar field.
 */
fun Date.get(field: Int): Int {
    return Calendar
        .getInstance(getTimeZone("UTC"))
        .let {
            it.time = this@get
            it.get(field)
        }
}

/**
 * Adds calendar field to current date.
 */
fun Date.add(
    field: Int,
    amount: Int
): Date {
    return Calendar
        .getInstance(getTimeZone("UTC"))
        .let {
            it.time = this@add
            it.add(
                field,
                amount
            )
            it.time
        }
}

/**
 * Sets calendar field to current date.
 */
fun Date.set(
    field: Int,
    amount: Int
): Date {
    return Calendar
        .getInstance(getTimeZone("UTC"))
        .let {
            it.time = this@set
            it.set(
                field,
                amount
            )
            it.time
        }
}
