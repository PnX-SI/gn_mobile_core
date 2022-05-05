package fr.geonature.datasync.util

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Unit tests about [String.parseAsDuration].
 *
 * @author S. Grimault
 */
class TimeUnitHelperKtTest {

    @Test
    fun `should parse valid duration string`() {
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
    }

    @Test
    fun `should parse anyway invalid duration string`() {
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

    @Test
    fun `should parse numbers to duration`() {
        assertEquals(
            15.toDuration(DurationUnit.SECONDS),
            15
                .toDuration(DurationUnit.SECONDS)
                .toIsoString()
                .parseAsDuration()
        )
        assertEquals(
            20.toDuration(DurationUnit.MINUTES),
            20
                .toDuration(DurationUnit.MINUTES)
                .toIsoString()
                .parseAsDuration()
        )
        assertEquals(
            15
                .toDuration(DurationUnit.MINUTES)
                .plus(30.toDuration(DurationUnit.SECONDS)),
            15
                .toDuration(DurationUnit.MINUTES)
                .plus(30.toDuration(DurationUnit.SECONDS))
                .toIsoString()
                .parseAsDuration()
        )

        assertEquals(
            2.toDuration(DurationUnit.HOURS),
            2
                .toDuration(DurationUnit.HOURS)
                .toIsoString()
                .parseAsDuration()
        )
        assertEquals(
            4
                .toDuration(DurationUnit.HOURS)
                .plus(30.toDuration(DurationUnit.MINUTES)),
            4
                .toDuration(DurationUnit.HOURS)
                .plus(30.toDuration(DurationUnit.MINUTES))
                .toIsoString()
                .parseAsDuration()
        )

        assertEquals(
            1.toDuration(DurationUnit.DAYS),
            1
                .toDuration(DurationUnit.DAYS)
                .toIsoString()
                .parseAsDuration()
        )
        assertEquals(
            1
                .toDuration(DurationUnit.DAYS)
                .plus(12.toDuration(DurationUnit.HOURS))
                .plus(15.toDuration(DurationUnit.MINUTES))
                .plus(30.toDuration(DurationUnit.SECONDS)),
            1
                .toDuration(DurationUnit.DAYS)
                .plus(12.toDuration(DurationUnit.HOURS))
                .plus(15.toDuration(DurationUnit.MINUTES))
                .plus(30.toDuration(DurationUnit.SECONDS))
                .toIsoString()
                .parseAsDuration()
        )
        assertEquals(
            130530.toDuration(DurationUnit.SECONDS),
            130530
                .toDuration(DurationUnit.SECONDS)
                .toIsoString()
                .parseAsDuration()
        )
    }
}