package fr.geonature.commons.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

    return runCatching {
        LocalDateTime.parse(
            str,
            DateTimeFormatter.ISO_DATE_TIME
        )
    }
        .recoverCatching {
            LocalDate
                .parse(
                    str,
                    DateTimeFormatter.ISO_DATE
                )
                .atStartOfDay()
        }
        .recoverCatching {
            LocalTime
                .parse(
                    str,
                    DateTimeFormatter.ISO_TIME
                )
                .atDate(LocalDate.now())
        }
        .map {
            Date.from(
                it
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            )
        }
        .getOrNull()
}

/**
 * Formats current date using the given pattern.
 */
fun Date.format(
    pattern: String,
    timeZone: TimeZone = TimeZone.getDefault()
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
fun Date.get(
    field: Int,
    timeZone: TimeZone = TimeZone.getDefault()
): Int {
    return Calendar
        .getInstance(timeZone)
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
    amount: Int,
    timeZone: TimeZone = TimeZone.getDefault()
): Date {
    return Calendar
        .getInstance(timeZone)
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
    amount: Int,
    timeZone: TimeZone = TimeZone.getDefault()
): Date {
    return Calendar
        .getInstance(timeZone)
        .let {
            it.time = this@set
            it.set(
                field,
                amount
            )
            it.time
        }
}
