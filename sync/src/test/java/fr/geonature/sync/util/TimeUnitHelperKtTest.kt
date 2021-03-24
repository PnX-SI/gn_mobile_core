package fr.geonature.sync.util

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

class TimeUnitHelperKtTest {

    @ExperimentalTime
    @Test
    fun testParse() {
        assertEquals(
            15.toDuration(DurationUnit.SECONDS),
            "15s".parseAsDuration()
        )
        assertEquals(
            15.toDuration(DurationUnit.SECONDS),
            "15S".parseAsDuration()
        )

        assertEquals(
            20.toDuration(DurationUnit.MINUTES),
            "20m".parseAsDuration()
        )
        assertEquals(
            20.toDuration(DurationUnit.MINUTES),
            "20M".parseAsDuration()
        )
        assertEquals(
            15
                .toDuration(DurationUnit.MINUTES)
                .plus(30.toDuration(DurationUnit.SECONDS)),
            "15m30s".parseAsDuration()
        )

        assertEquals(
            2.toDuration(DurationUnit.HOURS),
            "2h".parseAsDuration()
        )
        assertEquals(
            2.toDuration(DurationUnit.HOURS),
            "2H".parseAsDuration()
        )
        assertEquals(
            4
                .toDuration(DurationUnit.HOURS)
                .plus(30.toDuration(DurationUnit.MINUTES)),
            "4h30m".parseAsDuration()
        )

        assertEquals(
            1.toDuration(DurationUnit.DAYS),
            "1d".parseAsDuration()
        )
        assertEquals(
            1.toDuration(DurationUnit.DAYS),
            "1D".parseAsDuration()
        )
        assertEquals(
            1
                .toDuration(DurationUnit.DAYS)
                .plus(12.toDuration(DurationUnit.HOURS))
                .plus(15.toDuration(DurationUnit.MINUTES))
                .plus(30.toDuration(DurationUnit.SECONDS)),
            "1d12h15m30s".parseAsDuration()
        )
        assertEquals(
            130530.toDuration(DurationUnit.SECONDS),
            "1d12h15m30s".parseAsDuration()
        )

        assertEquals(
            1
                .toDuration(DurationUnit.DAYS)
                .plus(15.toDuration(DurationUnit.MINUTES))
                .plus(30.toDuration(DurationUnit.SECONDS)),
            "1dno_such_duration15m30s".parseAsDuration()
        )

        assertEquals(
            Duration.ZERO,
            "no_such_duration".parseAsDuration()
        )
    }
}