package fr.geonature.commons.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone.getTimeZone

/**
 * `Date` helpers.
 *
 * @author S. Grimault
 */

/**
 * Parses given string to [Date].
 */
fun toDate(str: String?): Date? {
    if (str.isNullOrBlank()) return null

    return try {
        SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            Locale.getDefault()
        ).apply { timeZone = getTimeZone("UTC") }
            .parse(str)
    } catch (pe: ParseException) {
        try {
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).apply { timeZone = getTimeZone("UTC") }
                .parse(str)
        } catch (pe: ParseException) {
            return null
        }
    }
}

/**
 * Formats current date to ISO-8601.
 */
fun Date.toIsoDateString(): String {
    return SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        Locale.getDefault()
    ).apply { timeZone = getTimeZone("UTC") }
        .format(this)
}

/**
 * Adds calendar field to current date.
 */
fun Date.add(field: Int, amount: Int): Date {
    Calendar.getInstance()
        .apply {
            time = this@add
            add(
                field,
                amount
            )
            return time
        }
}
