package fr.geonature.datasync.util

import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * `Duration` helpers.
 *
 * @author S. Grimault
 */

fun String.parseAsDuration(): Duration {
    return Regex("(\\d+)([smhd])")
        .findAll(lowercase(Locale.ROOT).removePrefix("pt"))
        .map { it.destructured.toList() }
        .map {
            Pair(
                it[0].toIntOrNull(),
                when (it[1]) {
                    "s" -> DurationUnit.SECONDS
                    "m" -> DurationUnit.MINUTES
                    "h" -> DurationUnit.HOURS
                    "d" -> DurationUnit.DAYS
                    else -> null
                }
            )
        }
        .map { pair -> pair.second?.let { pair.first?.toDuration(it) } }
        .filterNotNull()
        .fold(Duration.ZERO) { acc: Duration, duration: Duration -> acc.plus(duration) }
}
