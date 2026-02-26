package fr.geonature.commons.util

import android.os.Build
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

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    // common ISO-8601 date time format patterns
    val patterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",   // 2016-10-28T08:15:00.123+0000
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // 2016-10-28T08:15:00.123Z
        "yyyy-MM-dd'T'HH:mm:ssZ",       // 2016-10-28T08:15:00+0000
        "yyyy-MM-dd'T'HH:mm:ss'Z'",     // 2016-10-28T08:15:00Z
        "yyyy-MM-dd'T'HH:mm:ss",        // 2016-10-28T08:15:00
        "yyyy-MM-dd HH:mm:ss",          // 2016-10-28 08:15:00
        "yyyy-MM-dd",                   // 2016-10-28
        "HH:mm:ss.SSSZ",                // 08:15:00.123+0000
        "HH:mm:ss.SSS'Z'",              // 08:15:00.123Z
        "HH:mm:ssZ",                    // 08:15:00+0000
        "HH:mm:ss'Z'",                  // 08:15:00Z
        "HH:mm:ss",                     // 08:15:00
        "HH:mm",                        // 08:15
    )

    var normalizedString = str.trim()

    // handle timezone offset formats (e.g. convert +05:30 to +0530)
    normalizedString = normalizedString.replace(
        Regex("([+-]\\d{2}):(\\d{2})$"),
        "$1$2"
    )

    patterns.forEach { pattern ->
        runCatching {
            SimpleDateFormat(
                pattern,
                Locale.ROOT
            )
                .apply { isLenient = true }
                .parse(normalizedString)
        }
            .onFailure { return@forEach }
            .onSuccess { return it }
    }

    // null if no pattern matches
    return null
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
